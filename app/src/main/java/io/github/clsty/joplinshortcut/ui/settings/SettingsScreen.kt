package io.github.clsty.joplinshortcut.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import io.github.clsty.joplinshortcut.R
import io.github.clsty.joplinshortcut.data.db.entities.ApiConfigEntity


@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val apiConfigs by viewModel.apiConfigs.collectAsState()
    val layoutOrientation by viewModel.layoutOrientation.collectAsState()
    val hiddenNotes by viewModel.hiddenNotes.collectAsState()
    val hiddenNotebooks by viewModel.hiddenNotebooks.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val testResult by viewModel.testResult.collectAsState()

    var showAddConfigDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<ApiConfigEntity?>(null) }
    var hiddenExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // API Configurations section
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.settings_api_configs),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddConfigDialog = true }) {
                    Icon(Icons.Default.Add, stringResource(R.string.settings_add_config))
                }
            }
        }

        items(apiConfigs, key = { it.id }) { config ->
            ApiConfigCard(
                config = config,
                isSyncing = isSyncing,
                syncMessage = syncMessage,
                testResult = testResult,
                onEdit = { editingConfig = config },
                onDelete = { viewModel.deleteConfig(config) },
                onTest = { viewModel.testConnection(config) },
                onSync = { viewModel.syncNow(config) }
            )
        }

        item { HorizontalDivider() }

        // Layout Orientation
        item {
            Text(stringResource(R.string.settings_layout_orientation), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("auto" to R.string.layout_auto, "vertical" to R.string.layout_vertical, "horizontal" to R.string.layout_horizontal).forEach { (value, labelRes) ->
                    FilterChip(
                        selected = layoutOrientation == value,
                        onClick = { viewModel.setLayoutOrientation(value) },
                        label = { Text(stringResource(labelRes)) }
                    )
                }
            }
        }

        item { HorizontalDivider() }

        // Hidden Items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.settings_hidden_items),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text("${hiddenNotes.size + hiddenNotebooks.size}", style = MaterialTheme.typography.labelMedium)
                IconButton(onClick = { hiddenExpanded = !hiddenExpanded }) {
                    Icon(
                        if (hiddenExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null
                    )
                }
            }
        }
        item {
            AnimatedVisibility(visible = hiddenExpanded) {
                Column {
                    if (hiddenNotebooks.isNotEmpty()) {
                        Text("Notebooks", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 4.dp))
                        hiddenNotebooks.forEach { nb ->
                            HiddenItemRow(title = nb.title, icon = Icons.Default.Folder) {
                                viewModel.restoreNotebook(nb.id)
                            }
                        }
                    }
                    if (hiddenNotes.isNotEmpty()) {
                        Text("Notes", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 4.dp))
                        hiddenNotes.forEach { note ->
                            HiddenItemRow(title = note.title, icon = Icons.Default.Note) {
                                viewModel.restoreNote(note.id)
                            }
                        }
                    }
                    if (hiddenNotes.isEmpty() && hiddenNotebooks.isEmpty()) {
                        Text("No hidden items", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item { HorizontalDivider() }

        // About
        item {
            Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.titleMedium)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.about_version, "1.0.0"))
                Text(stringResource(R.string.about_description))
            }
        }
    }

    if (showAddConfigDialog) {
        ApiConfigDialog(
            config = null,
            onDismiss = { showAddConfigDialog = false },
            onSave = { viewModel.saveConfig(it); showAddConfigDialog = false }
        )
    }
    editingConfig?.let { config ->
        ApiConfigDialog(
            config = config,
            onDismiss = { editingConfig = null },
            onSave = { viewModel.saveConfig(it); editingConfig = null }
        )
    }
}

@Composable
fun HiddenItemRow(title: String, icon: ImageVector, onRestore: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRestore) { Text(stringResource(R.string.action_restore)) }
    }
}

@Composable
fun ApiConfigCard(
    config: ApiConfigEntity,
    isSyncing: Boolean,
    syncMessage: String?,
    testResult: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    onSync: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(config.name, style = MaterialTheme.typography.bodyLarge)
                    Text(config.baseUrl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Sync: ${config.syncMode}${if (config.syncMode == "periodic") " every ${config.syncIntervalMinutes}min" else ""}",
                        style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onTest, modifier = Modifier.weight(1f)) {
                    Text("Test")
                }
                Button(
                    onClick = onSync,
                    enabled = !isSyncing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSyncing) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    else Text(stringResource(R.string.action_sync))
                }
            }
            testResult?.let { Text("Connection: $it", style = MaterialTheme.typography.bodySmall) }
            syncMessage?.let { Text("Sync: $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun ApiConfigDialog(
    config: ApiConfigEntity?,
    onDismiss: () -> Unit,
    onSave: (ApiConfigEntity) -> Unit
) {
    var name by remember { mutableStateOf(config?.name ?: "") }
    var baseUrl by remember { mutableStateOf(config?.baseUrl ?: "") }
    var token by remember { mutableStateOf(config?.token ?: "") }
    var syncMode by remember { mutableStateOf(config?.syncMode ?: "manual") }
    var intervalMinutes by remember { mutableStateOf(config?.syncIntervalMinutes?.toString() ?: "60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (config == null) stringResource(R.string.settings_add_config) else "Edit Config") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.settings_config_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = baseUrl, onValueChange = { baseUrl = it },
                    label = { Text(stringResource(R.string.settings_config_url)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = token, onValueChange = { token = it },
                    label = { Text(stringResource(R.string.settings_config_token)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.settings_config_sync_mode))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = syncMode == "manual", onClick = { syncMode = "manual" },
                        label = { Text(stringResource(R.string.settings_sync_manual)) })
                    FilterChip(selected = syncMode == "periodic", onClick = { syncMode = "periodic" },
                        label = { Text(stringResource(R.string.settings_sync_periodic)) })
                }
                if (syncMode == "periodic") {
                    OutlinedTextField(value = intervalMinutes, onValueChange = { intervalMinutes = it },
                        label = { Text(stringResource(R.string.settings_config_sync_interval)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(ApiConfigEntity(
                        id = config?.id ?: 0L,
                        name = name,
                        baseUrl = baseUrl,
                        token = token,
                        syncMode = syncMode,
                        syncIntervalMinutes = intervalMinutes.toIntOrNull() ?: 60,
                        isActive = config?.isActive ?: true
                    ))
                },
                enabled = name.isNotBlank() && baseUrl.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
