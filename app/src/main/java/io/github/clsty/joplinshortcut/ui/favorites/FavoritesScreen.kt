package io.github.clsty.joplinshortcut.ui.favorites

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.clsty.joplinshortcut.R
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteEntity
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteItemEntity
import io.github.clsty.joplinshortcut.ui.settings.SettingsViewModel

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val selectedFavoriteId by viewModel.selectedFavoriteId.collectAsState()
    val items by viewModel.selectedFavoriteItems.collectAsState()
    val layoutOrientation by settingsViewModel.layoutOrientation.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val useHorizontal = when (layoutOrientation) {
        "horizontal" -> true
        "vertical" -> false
        else -> isLandscape
    }

    var showCreateDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.nav_favorites),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, stringResource(R.string.create_favorite))
            }
        }

        if (useHorizontal) {
            Row(modifier = Modifier.fillMaxSize()) {
                FavoritesList(
                    modifier = Modifier.weight(0.4f).fillMaxHeight(),
                    favorites = favorites,
                    selectedId = selectedFavoriteId,
                    onSelect = { viewModel.selectFavorite(it) },
                    onDelete = { viewModel.deleteFavorite(it) }
                )
                VerticalDivider()
                FavoriteItemsList(
                    modifier = Modifier.weight(0.6f).fillMaxHeight(),
                    items = items,
                    favoriteId = selectedFavoriteId,
                    onRemove = { favId, itemId -> viewModel.removeItem(favId, itemId) }
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                FavoritesList(
                    modifier = Modifier.weight(0.4f).fillMaxWidth(),
                    favorites = favorites,
                    selectedId = selectedFavoriteId,
                    onSelect = { viewModel.selectFavorite(it) },
                    onDelete = { viewModel.deleteFavorite(it) }
                )
                HorizontalDivider()
                FavoriteItemsList(
                    modifier = Modifier.weight(0.6f).fillMaxWidth(),
                    items = items,
                    favoriteId = selectedFavoriteId,
                    onRemove = { favId, itemId -> viewModel.removeItem(favId, itemId) }
                )
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.create_favorite)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(R.string.new_favorite_name_hint)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.createFavorite(name); showCreateDialog = false },
                    enabled = name.isNotBlank()
                ) { Text(stringResource(R.string.create_favorite)) }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesList(
    modifier: Modifier,
    favorites: List<FavoriteEntity>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onDelete: (FavoriteEntity) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(favorites, key = { it.id }) { fav ->
            var offsetX by remember { mutableStateOf(0f) }
            val bgColor = if (selectedId == fav.id) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > 100f) onDelete(fav)
                                offsetX = 0f
                            }
                        ) { _, d -> offsetX += d }
                    }
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor)
                    .combinedClickable(onClick = { onSelect(fav.id) })
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(fav.name, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                IconButton(onClick = { onDelete(fav) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FavoriteItemsList(
    modifier: Modifier,
    items: List<FavoriteItemEntity>,
    favoriteId: Long?,
    onRemove: (Long, String) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(modifier = modifier) {
        if (favoriteId == null) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Select a favorites list", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(items, key = { it.id }) { item ->
                FavoriteItemRow(
                    item = item,
                    favoriteId = favoriteId,
                    onRemove = onRemove,
                    context = context
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoriteItemRow(
    item: FavoriteItemEntity,
    favoriteId: Long,
    onRemove: (Long, String) -> Unit,
    context: Context
) {
    var offsetX by remember { mutableStateOf(0f) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > 100f) onRemove(favoriteId, item.itemId)
                        offsetX = 0f
                    }
                ) { _, d -> offsetX += d }
            }
            .combinedClickable(onClick = {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("joplin://${item.itemType}/${item.itemId}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            })
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (item.itemType == "notebook") Icons.Default.Folder else Icons.Default.Note,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(item.title, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(onClick = { onRemove(favoriteId, item.itemId) }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}
