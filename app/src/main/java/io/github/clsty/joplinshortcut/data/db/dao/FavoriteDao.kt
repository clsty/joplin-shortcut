package io.github.clsty.joplinshortcut.data.db.dao

import androidx.room.*
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteEntity
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY name ASC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(favorite: FavoriteEntity): Long

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM favorite_items WHERE favoriteId = :favoriteId ORDER BY title ASC")
    fun getItems(favoriteId: Long): Flow<List<FavoriteItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: FavoriteItemEntity)

    @Query("DELETE FROM favorite_items WHERE favoriteId = :favoriteId AND itemId = :itemId")
    suspend fun removeItem(favoriteId: Long, itemId: String)

    @Query("DELETE FROM favorite_items WHERE favoriteId = :favoriteId")
    suspend fun clearItems(favoriteId: Long)

    @Query("SELECT * FROM favorites WHERE id = :id")
    suspend fun getById(id: Long): FavoriteEntity?
}
