package io.github.clsty.joplinshortcut.ui.manage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.clsty.joplinshortcut.R
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteEntity
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import io.github.clsty.joplinshortcut.data.db.entities.WidgetConfigEntity
import io.github.clsty.joplinshortcut.ui.widget.WidgetManagementViewModel

@Composable
fun ManageScreen(viewModel: WidgetManagementViewModel = hiltViewModel()) {
    val widgetConfigs by viewModel.widgetConfigs.collectAsState()
    val notebooks by viewModel.notebooks.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, stringResource(R.string.action_create_widget))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                stringResource(R.string.nav_manage),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )

            if (widgetConfigs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No widgets configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn {
                    items(widgetConfigs, key = { it.id }) { config ->
                        WidgetConfigItem(
                            config = config,
                            onDelete = { viewModel.deleteWidgetConfig(config) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWidgetDialog(
            notebooks = notebooks,
            favorites = favorites,
            onDismiss = { showAddDialog = false },
            onSave = { config ->
                viewModel.saveWidgetConfig(config)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WidgetConfigItem(config: WidgetConfigEntity, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Dashboard, null)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(config.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${config.sourceType}: ${config.sourceId} (depth: ${config.recursiveDepth})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
        }
    }
}

@Composable
fun AddWidgetDialog(
    notebooks: List<NotebookEntity>,
    favorites: List<FavoriteEntity>,
    onDismiss: () -> Unit,
    onSave: (WidgetConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sourceType by remember { mutableStateOf("notebook") }
    var selectedSourceId by remember { mutableStateOf("") }
    var depth by remember { mutableStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.widget_configure_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Widget Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    FilterChip(
                        selected = sourceType == "notebook",
                        onClick = { sourceType = "notebook"; selectedSourceId = "" },
                        label = { Text("Notebook") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = sourceType == "favorite",
                        onClick = { sourceType = "favorite"; selectedSourceId = "" },
                        label = { Text("Favorites") }
                    )
                }
                if (sourceType == "notebook") {
                    notebooks.forEach { nb ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedSourceId == nb.id,
                                onClick = { selectedSourceId = nb.id }
                            )
                            Text(nb.title)
                        }
                    }
                } else {
                    favorites.forEach { fav ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedSourceId == fav.id.toString(),
                                onClick = { selectedSourceId = fav.id.toString() }
                            )
                            Text(fav.name)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Depth: $depth")
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { if (depth > 1) depth-- }) { Icon(Icons.Default.Remove, null) }
                    IconButton(onClick = { if (depth < 5) depth++ }) { Icon(Icons.Default.Add, null) }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedSourceId.isNotEmpty()) {
                        onSave(WidgetConfigEntity(
                            widgetId = 0,
                            name = name,
                            sourceType = sourceType,
                            sourceId = selectedSourceId,
                            recursiveDepth = depth
                        ))
                    }
                },
                enabled = name.isNotBlank() && selectedSourceId.isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
