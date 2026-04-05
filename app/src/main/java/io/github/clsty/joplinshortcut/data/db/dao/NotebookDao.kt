package io.github.clsty.joplinshortcut.data.db.dao

import androidx.room.*
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {
    @Query("SELECT * FROM notebooks WHERE isHidden = 0 ORDER BY title ASC")
    fun getAllVisible(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks ORDER BY title ASC")
    fun getAll(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE parentId = :parentId AND isHidden = 0 ORDER BY title ASC")
    fun getChildren(parentId: String): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE (parentId = '' OR parentId IS NULL) AND isHidden = 0 ORDER BY title ASC")
    fun getTopLevel(): Flow<List<NotebookEntity>>

    @Query("SELECT * FROM notebooks WHERE title LIKE '%' || :query || '%' AND isHidden = 0 ORDER BY title ASC")
    fun search(query: String): Flow<List<NotebookEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notebooks: List<NotebookEntity>)

    @Query("UPDATE notebooks SET isHidden = :hidden WHERE id = :id")
    suspend fun setHidden(id: String, hidden: Boolean)

    @Query("DELETE FROM notebooks")
    suspend fun deleteAll()

    @Query("SELECT * FROM notebooks WHERE isHidden = 1 ORDER BY title ASC")
    fun getHidden(): Flow<List<NotebookEntity>>
}
