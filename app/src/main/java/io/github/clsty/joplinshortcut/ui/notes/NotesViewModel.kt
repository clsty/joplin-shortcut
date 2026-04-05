package io.github.clsty.joplinshortcut.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.clsty.joplinshortcut.data.api.JoplinRepository
import io.github.clsty.joplinshortcut.data.db.dao.ApiConfigDao
import io.github.clsty.joplinshortcut.data.db.dao.NoteDao
import io.github.clsty.joplinshortcut.data.db.dao.NotebookDao
import io.github.clsty.joplinshortcut.data.db.dao.FavoriteDao
import io.github.clsty.joplinshortcut.data.db.entities.NoteEntity
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteEntity
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotebookTreeNode(
    val notebook: NotebookEntity,
    val children: List<NotebookTreeNode>,
    val isExpanded: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val notebookDao: NotebookDao,
    private val apiConfigDao: ApiConfigDao,
    private val favoriteDao: FavoriteDao,
    private val repository: JoplinRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedNotebookId = MutableStateFlow<String?>(null)
    val selectedNoteIds = MutableStateFlow<Set<String>>(emptySet())
    val expandedNotebookIds = MutableStateFlow<Set<String>>(emptySet())
    val isSyncing = MutableStateFlow(false)
    val syncError = MutableStateFlow<String?>(null)

    val isSelectionMode: StateFlow<Boolean> = selectedNoteIds
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val allNotebooks: StateFlow<List<NotebookEntity>> = notebookDao.getAllVisible()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notebookTree: StateFlow<List<NotebookTreeNode>> = combine(
        allNotebooks, expandedNotebookIds
    ) { notebooks, expanded ->
        buildTree(notebooks, "", expanded)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = combine(
        selectedNotebookId, searchQuery
    ) { notebookId, query ->
        Pair(notebookId, query)
    }.flatMapLatest { (notebookId, query) ->
        when {
            query.isNotBlank() -> noteDao.search(query)
            notebookId != null -> noteDao.getByNotebook(notebookId)
            else -> noteDao.getAllVisible()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = favoriteDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun buildTree(
        notebooks: List<NotebookEntity>,
        parentId: String,
        expanded: Set<String>
    ): List<NotebookTreeNode> {
        return notebooks
            .filter { it.parentId == parentId }
            .map { nb ->
                NotebookTreeNode(
                    notebook = nb,
                    children = if (expanded.contains(nb.id))
                        buildTree(notebooks, nb.id, expanded)
                    else emptyList(),
                    isExpanded = expanded.contains(nb.id)
                )
            }
    }

    fun selectNotebook(id: String?) {
        selectedNotebookId.value = id
        selectedNoteIds.value = emptySet()
    }

    fun toggleExpanded(notebookId: String) {
        val current = expandedNotebookIds.value
        expandedNotebookIds.value = if (current.contains(notebookId))
            current - notebookId else current + notebookId
    }

    fun toggleNoteSelection(id: String) {
        val current = selectedNoteIds.value
        selectedNoteIds.value = if (current.contains(id)) current - id else current + id
    }

    fun clearSelection() {
        selectedNoteIds.value = emptySet()
    }

    fun selectAll(noteList: List<NoteEntity>) {
        selectedNoteIds.value = noteList.map { it.id }.toSet()
    }

    fun invertSelection(noteList: List<NoteEntity>) {
        val all = noteList.map { it.id }.toSet()
        selectedNoteIds.value = all - selectedNoteIds.value
    }

    fun hideNote(id: String) {
        viewModelScope.launch {
            noteDao.setHidden(id, true)
            selectedNoteIds.value = selectedNoteIds.value - id
        }
    }

    fun hideSelectedNotes() {
        viewModelScope.launch {
            selectedNoteIds.value.forEach { noteDao.setHidden(it, true) }
            selectedNoteIds.value = emptySet()
        }
    }

    fun hideNotebook(id: String) {
        viewModelScope.launch { notebookDao.setHidden(id, true) }
    }

    fun addNotesToFavorite(favoriteId: Long, noteIds: Set<String>, noteTitles: Map<String, String>) {
        viewModelScope.launch {
            noteIds.forEach { id ->
                favoriteDao.insertItem(
                    FavoriteItemEntity(
                        favoriteId = favoriteId,
                        itemId = id,
                        itemType = "note",
                        title = noteTitles[id] ?: ""
                    )
                )
            }
        }
    }

    fun createFavoriteAndAdd(name: String, noteIds: Set<String>, noteTitles: Map<String, String>) {
        viewModelScope.launch {
            val id = favoriteDao.insert(FavoriteEntity(name = name))
            noteIds.forEach { noteId ->
                favoriteDao.insertItem(
                    FavoriteItemEntity(
                        favoriteId = id,
                        itemId = noteId,
                        itemType = "note",
                        title = noteTitles[noteId] ?: ""
                    )
                )
            }
        }
    }

    fun addNotebookToFavorite(favoriteId: Long, notebookId: String, title: String) {
        viewModelScope.launch {
            favoriteDao.insertItem(
                FavoriteItemEntity(
                    favoriteId = favoriteId,
                    itemId = notebookId,
                    itemType = "notebook",
                    title = title
                )
            )
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            isSyncing.value = true
            syncError.value = null
            try {
                val configs = apiConfigDao.getAll().first()
                if (configs.isEmpty()) {
                    syncError.value = "No API configurations found"
                    return@launch
                }
                configs.forEach { config ->
                    repository.syncFromServer(config).onFailure { e ->
                        syncError.value = e.message
                    }
                }
            } finally {
                isSyncing.value = false
            }
        }
    }
}
