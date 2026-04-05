package io.github.clsty.joplinshortcut.data.db.dao

import androidx.room.*
import io.github.clsty.joplinshortcut.data.db.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isHidden = 0 ORDER BY title ASC")
    fun getAllVisible(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY title ASC")
    fun getAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE parentId = :notebookId AND isHidden = 0 ORDER BY title ASC")
    fun getByNotebook(notebookId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' AND isHidden = 0 ORDER BY title ASC")
    fun search(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("UPDATE notes SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    @Query("SELECT * FROM notes WHERE isHidden = 1 ORDER BY title ASC")
    fun getHidden(): Flow<List<NoteEntity>>
}
