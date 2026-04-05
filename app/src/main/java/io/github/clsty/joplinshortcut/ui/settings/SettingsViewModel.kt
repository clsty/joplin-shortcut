package io.github.clsty.joplinshortcut.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.clsty.joplinshortcut.data.api.JoplinRepository
import io.github.clsty.joplinshortcut.data.db.dao.ApiConfigDao
import io.github.clsty.joplinshortcut.data.db.dao.NoteDao
import io.github.clsty.joplinshortcut.data.db.dao.NotebookDao
import io.github.clsty.joplinshortcut.data.db.entities.ApiConfigEntity
import io.github.clsty.joplinshortcut.data.db.entities.NoteEntity
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import io.github.clsty.joplinshortcut.worker.SyncWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val LAYOUT_ORIENTATION_KEY = stringPreferencesKey("layout_orientation")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiConfigDao: ApiConfigDao,
    private val noteDao: NoteDao,
    private val notebookDao: NotebookDao,
    private val repository: JoplinRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val apiConfigs: StateFlow<List<ApiConfigEntity>> = apiConfigDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val layoutOrientation: StateFlow<String> = context.dataStore.data
        .map { prefs -> prefs[LAYOUT_ORIENTATION_KEY] ?: "auto" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "auto")

    val hiddenNotes: StateFlow<List<NoteEntity>> = noteDao.getHidden()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hiddenNotebooks: StateFlow<List<NotebookEntity>> = notebookDao.getHidden()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSyncing = MutableStateFlow(false)
    val syncMessage = MutableStateFlow<String?>(null)
    val testResult = MutableStateFlow<String?>(null)

    fun saveConfig(config: ApiConfigEntity) {
        viewModelScope.launch {
            val savedId = if (config.id == 0L) {
                apiConfigDao.insert(config)
            } else {
                apiConfigDao.update(config)
                config.id
            }
            if (config.syncMode == "periodic") {
                schedulePeriodicSync(savedId, config.syncIntervalMinutes)
            } else {
                cancelPeriodicSync(savedId)
            }
        }
    }

    fun deleteConfig(config: ApiConfigEntity) {
        viewModelScope.launch {
            cancelPeriodicSync(config.id)
            apiConfigDao.delete(config)
        }
    }

    fun testConnection(config: ApiConfigEntity) {
        viewModelScope.launch {
            testResult.value = null
            repository.testConnection(config)
                .onSuccess { testResult.value = "success" }
                .onFailure { testResult.value = "failed: ${it.message}" }
        }
    }

    fun syncNow(config: ApiConfigEntity) {
        viewModelScope.launch {
            isSyncing.value = true
            syncMessage.value = null
            repository.syncFromServer(config)
                .onSuccess { syncMessage.value = "success" }
                .onFailure { syncMessage.value = "failed: ${it.message}" }
            isSyncing.value = false
        }
    }

    fun setLayoutOrientation(orientation: String) {
        viewModelScope.launch {
            context.dataStore.edit { prefs -> prefs[LAYOUT_ORIENTATION_KEY] = orientation }
        }
    }

    fun restoreNote(id: String) {
        viewModelScope.launch { noteDao.setHidden(id, false) }
    }

    fun restoreNotebook(id: String) {
        viewModelScope.launch { notebookDao.setHidden(id, false) }
    }

    private fun schedulePeriodicSync(configId: Long, intervalMinutes: Int) {
        val workManager = WorkManager.getInstance(context)
        val request = SyncWorker.buildPeriodicRequest(configId, intervalMinutes)
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.getWorkName(configId),
            androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelPeriodicSync(configId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(SyncWorker.getWorkName(configId))
    }
}
