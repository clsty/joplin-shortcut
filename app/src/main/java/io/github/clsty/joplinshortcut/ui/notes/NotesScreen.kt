package io.github.clsty.joplinshortcut.ui.notes

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
import io.github.clsty.joplinshortcut.data.db.entities.NoteEntity
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import io.github.clsty.joplinshortcut.ui.settings.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    viewModel: NotesViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val notebookTree by viewModel.notebookTree.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNotebookId by viewModel.selectedNotebookId.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val layoutOrientation by settingsViewModel.layoutOrientation.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val useHorizontal = when (layoutOrientation) {
        "horizontal" -> true
        "vertical" -> false
        else -> isLandscape
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top bar
        if (isSelectionMode) {
            SelectionToolbar(
                selectedCount = selectedNoteIds.size,
                onClear = viewModel::clearSelection,
                onSelectAll = { viewModel.selectAll(notes) },
                onInvert = { viewModel.invertSelection(notes) },
                onHide = viewModel::hideSelectedNotes,
                onAddToFavorites = { /* handled by dialog below */ },
                notes = notes,
                favorites = favorites,
                viewModel = viewModel
            )
        } else {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.searchQuery.value = it },
                isSyncing = isSyncing,
                onSync = viewModel::syncNow
            )
        }

        if (useHorizontal) {
            Row(modifier = Modifier.fillMaxSize()) {
                NotebookTreePanel(
                    modifier = Modifier.weight(0.35f).fillMaxHeight(),
                    tree = notebookTree,
                    selectedId = selectedNotebookId,
                    favorites = favorites,
                    viewModel = viewModel
                )
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))
                NoteListPanel(
                    modifier = Modifier.weight(0.65f).fillMaxHeight(),
                    notes = notes,
                    selectedIds = selectedNoteIds,
                    favorites = favorites,
                    viewModel = viewModel
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                NotebookTreePanel(
                    modifier = Modifier.weight(0.35f).fillMaxWidth(),
                    tree = notebookTree,
                    selectedId = selectedNotebookId,
                    favorites = favorites,
                    viewModel = viewModel
                )
                Divider()
                NoteListPanel(
                    modifier = Modifier.weight(0.65f).fillMaxWidth(),
                    notes = notes,
                    selectedIds = selectedNoteIds,
                    favorites = favorites,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSyncing: Boolean,
    onSync: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.notes_search_hint)) },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = if (query.isNotEmpty()) {
                { IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Default.Clear, null) } }
            } else null
        )
        Spacer(Modifier.width(8.dp))
        if (isSyncing) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            IconButton(onClick = onSync) { Icon(Icons.Default.Sync, stringResource(R.string.action_sync)) }
        }
    }
}

@Composable
fun SelectionToolbar(
    selectedCount: Int,
    onClear: () -> Unit,
    onSelectAll: () -> Unit,
    onInvert: () -> Unit,
    onHide: () -> Unit,
    onAddToFavorites: () -> Unit,
    notes: List<NoteEntity>,
    favorites: List<FavoriteEntity>,
    viewModel: NotesViewModel
) {
    var showFavDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClear) { Icon(Icons.Default.Close, null) }
        Text("$selectedCount selected", modifier = Modifier.weight(1f))
        IconButton(onClick = onSelectAll) { Icon(Icons.Default.SelectAll, stringResource(R.string.select_all)) }
        IconButton(onClick = onInvert) { Icon(Icons.Default.FlipToBack, stringResource(R.string.invert_selection)) }
        IconButton(onClick = { showFavDialog = true }) { Icon(Icons.Default.Star, stringResource(R.string.add_to_favorites)) }
        IconButton(onClick = onHide) { Icon(Icons.Default.VisibilityOff, stringResource(R.string.action_hide)) }
    }
    if (showFavDialog) {
        val selectedIds by viewModel.selectedNoteIds.collectAsState()
        val noteTitles = notes.associate { it.id to it.title }
        AddToFavoritesDialog(
            favorites = favorites,
            onDismiss = { showFavDialog = false },
            onAddToExisting = { favoriteId ->
                viewModel.addNotesToFavorite(favoriteId, selectedIds, noteTitles)
                showFavDialog = false
            },
            onCreateNew = { name ->
                viewModel.createFavoriteAndAdd(name, selectedIds, noteTitles)
                showFavDialog = false
            }
        )
    }
}

@Composable
fun AddToFavoritesDialog(
    favorites: List<FavoriteEntity>,
    onDismiss: () -> Unit,
    onAddToExisting: (Long) -> Unit,
    onCreateNew: (String) -> Unit
) {
    var newFavName by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_favorites)) },
        text = {
            Column {
                favorites.forEach { fav ->
                    TextButton(onClick = { onAddToExisting(fav.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(fav.name)
                    }
                }
                HorizontalDivider()
                if (showCreate) {
                    OutlinedTextField(
                        value = newFavName,
                        onValueChange = { newFavName = it },
                        placeholder = { Text(stringResource(R.string.new_favorite_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { if (newFavName.isNotBlank()) onCreateNew(newFavName) },
                        enabled = newFavName.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(R.string.create_favorite)) }
                } else {
                    TextButton(onClick = { showCreate = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null)
                        Text(stringResource(R.string.settings_add_config))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun NotebookTreePanel(
    modifier: Modifier,
    tree: List<NotebookTreeNode>,
    selectedId: String?,
    favorites: List<FavoriteEntity>,
    viewModel: NotesViewModel
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.notebook_tree_label),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                NotebookItem(
                    notebook = null,
                    depth = 0,
                    isSelected = selectedId == null,
                    onClick = { viewModel.selectNotebook(null) },
                    onExpand = {},
                    isExpanded = false,
                    hasChildren = false,
                    favorites = favorites,
                    viewModel = viewModel,
                    context = context
                )
            }
            fun renderTree(nodes: List<NotebookTreeNode>, depth: Int) {
                nodes.forEach { node ->
                    item(key = node.notebook.id) {
                        NotebookItem(
                            notebook = node.notebook,
                            depth = depth,
                            isSelected = selectedId == node.notebook.id,
                            onClick = { viewModel.selectNotebook(node.notebook.id) },
                            onExpand = { viewModel.toggleExpanded(node.notebook.id) },
                            isExpanded = node.isExpanded,
                            hasChildren = node.notebook.id.isNotEmpty(),
                            favorites = favorites,
                            viewModel = viewModel,
                            context = context
                        )
                    }
                    if (node.isExpanded) renderTree(node.children, depth + 1)
                }
            }
            renderTree(tree, 1)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotebookItem(
    notebook: NotebookEntity?,
    depth: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    onExpand: () -> Unit,
    isExpanded: Boolean,
    hasChildren: Boolean,
    favorites: List<FavoriteEntity>,
    viewModel: NotesViewModel,
    context: Context
) {
    var showMenu by remember { mutableStateOf(false) }
    var showFavDialog by remember { mutableStateOf(false) }
    var showProperties by remember { mutableStateOf(false) }

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .combinedClickable(onClick = onClick, onLongClick = { if (notebook != null) showMenu = true })
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (notebook == null) {
            Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("All Notes", style = MaterialTheme.typography.bodyMedium)
        } else {
            if (hasChildren) {
                IconButton(onClick = onExpand, modifier = Modifier.size(20.dp)) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, modifier = Modifier.size(16.dp)
                    )
                }
            } else Spacer(Modifier.width(20.dp))
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.Folder, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(notebook.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    if (notebook != null) {
        if (showMenu) {
            DropdownMenu(expanded = true, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_jump_to_joplin)) },
                    onClick = { openJoplin(context, "notebook", notebook.id); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.OpenInNew, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_favorite)) },
                    onClick = { showFavDialog = true; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Star, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_create_shortcut)) },
                    onClick = {
                        io.github.clsty.joplinshortcut.shortcut.ShortcutHelper.createShortcut(
                            context, notebook.id, notebook.title, notebook.id, "notebook"
                        )
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.AddLink, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_hide)) },
                    onClick = { viewModel.hideNotebook(notebook.id); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.VisibilityOff, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_properties)) },
                    onClick = { showProperties = true; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Info, null) }
                )
            }
        }
        if (showFavDialog) {
            AddToFavoritesDialog(
                favorites = favorites,
                onDismiss = { showFavDialog = false },
                onAddToExisting = { favId ->
                    viewModel.addNotebookToFavorite(favId, notebook.id, notebook.title)
                    showFavDialog = false
                },
                onCreateNew = { name ->
                    viewModel.createFavoriteAndAdd(name, setOf(notebook.id), mapOf(notebook.id to notebook.title))
                    showFavDialog = false
                }
            )
        }
        if (showProperties) {
            PropertiesDialog(
                title = notebook.title,
                id = notebook.id,
                parentId = notebook.parentId,
                updatedTime = notebook.updatedTime,
                isNotebook = true,
                onDismiss = { showProperties = false }
            )
        }
    }
}

@Composable
fun NoteListPanel(
    modifier: Modifier,
    notes: List<NoteEntity>,
    selectedIds: Set<String>,
    favorites: List<FavoriteEntity>,
    viewModel: NotesViewModel
) {
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.notes_list_label),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(notes, key = { it.id }) { note ->
                NoteItem(
                    note = note,
                    isSelected = selectedIds.contains(note.id),
                    onClick = { viewModel.toggleNoteSelection(note.id) },
                    favorites = favorites,
                    viewModel = viewModel,
                    context = context
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    favorites: List<FavoriteEntity>,
    viewModel: NotesViewModel,
    context: Context
) {
    var showMenu by remember { mutableStateOf(false) }
    var showFavDialog by remember { mutableStateOf(false) }
    var showProperties by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }

    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX < -100f -> openJoplin(context, "note", note.id)
                            offsetX > 100f -> viewModel.hideNote(note.id)
                        }
                        offsetX = 0f
                    }
                ) { _, dragAmount -> offsetX += dragAmount }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(bgColor)
                .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.Note, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (note.updatedTime > 0) {
                    Text(
                        formatDate(note.updatedTime),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (showMenu) {
            DropdownMenu(expanded = true, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_jump_to_joplin)) },
                    onClick = { openJoplin(context, "note", note.id); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.OpenInNew, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_favorite)) },
                    onClick = { showFavDialog = true; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Star, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_create_shortcut)) },
                    onClick = {
                        io.github.clsty.joplinshortcut.shortcut.ShortcutHelper.createShortcut(
                            context, note.id, note.title, note.id, "note"
                        )
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.AddLink, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_hide)) },
                    onClick = { viewModel.hideNote(note.id); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.VisibilityOff, null) }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_properties)) },
                    onClick = { showProperties = true; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Info, null) }
                )
            }
        }
        if (showFavDialog) {
            AddToFavoritesDialog(
                favorites = favorites,
                onDismiss = { showFavDialog = false },
                onAddToExisting = { favId ->
                    viewModel.addNotesToFavorite(favId, setOf(note.id), mapOf(note.id to note.title))
                    showFavDialog = false
                },
                onCreateNew = { name ->
                    viewModel.createFavoriteAndAdd(name, setOf(note.id), mapOf(note.id to note.title))
                    showFavDialog = false
                }
            )
        }
        if (showProperties) {
            PropertiesDialog(
                title = note.title,
                id = note.id,
                parentId = note.parentId,
                updatedTime = note.updatedTime,
                isNotebook = false,
                onDismiss = { showProperties = false }
            )
        }
    }
}

@Composable
fun PropertiesDialog(
    title: String,
    id: String,
    parentId: String,
    updatedTime: Long,
    isNotebook: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.action_properties)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Title: $title")
                Text("ID: $id")
                if (!isNotebook) Text("Notebook ID: $parentId")
                if (updatedTime > 0) Text("Updated: ${formatDate(updatedTime)}")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } }
    )
}

fun openJoplin(context: Context, type: String, id: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("joplin://$type/$id"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}

fun formatDate(epochMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(epochMs))
