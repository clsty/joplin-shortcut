package io.github.clsty.joplinshortcut.data.api

import io.github.clsty.joplinshortcut.data.db.dao.ApiConfigDao
import io.github.clsty.joplinshortcut.data.db.dao.NoteDao
import io.github.clsty.joplinshortcut.data.db.dao.NotebookDao
import io.github.clsty.joplinshortcut.data.db.entities.ApiConfigEntity
import io.github.clsty.joplinshortcut.data.db.entities.NoteEntity
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JoplinRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val notebookDao: NotebookDao,
    private val apiConfigDao: ApiConfigDao
) {
    suspend fun syncFromServer(config: ApiConfigEntity): Result<Unit> = runCatching {
        val api = RetrofitBuilder.create(config.baseUrl)

        // Fetch all notebooks
        val allFolders = mutableListOf<JoplinFolderDto>()
        var page = 1
        do {
            val response = api.getFolders(token = config.token, page = page)
            allFolders.addAll(response.items)
            page++
            if (!response.has_more) break
        } while (true)

        // Fetch all notes
        val allNotes = mutableListOf<JoplinNoteDto>()
        page = 1
        do {
            val response = api.getNotes(token = config.token, page = page)
            allNotes.addAll(response.items)
            page++
            if (!response.has_more) break
        } while (true)

        val notebookEntities = allFolders.map {
            NotebookEntity(
                id = it.id,
                title = it.title,
                parentId = it.parent_id,
                updatedTime = it.updated_time
            )
        }
        val noteEntities = allNotes.map {
            NoteEntity(
                id = it.id,
                title = it.title,
                parentId = it.parent_id,
                updatedTime = it.updated_time
            )
        }

        notebookDao.deleteAll()
        notebookDao.insertAll(notebookEntities)
        noteDao.deleteAll()
        noteDao.insertAll(noteEntities)
    }

    suspend fun syncConfig(configId: Long): Unit {
        val config = apiConfigDao.getById(configId) ?: return
        syncFromServer(config).getOrThrow()
    }

    suspend fun testConnection(config: ApiConfigEntity): Result<Unit> = runCatching {
        val api = RetrofitBuilder.create(config.baseUrl)
        api.getFolders(token = config.token, limit = 1, page = 1)
    }
}
