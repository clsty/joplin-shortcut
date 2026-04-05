package io.github.clsty.joplinshortcut.data.db.dao

import androidx.room.*
import io.github.clsty.joplinshortcut.data.db.entities.ApiConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiConfigDao {
    @Query("SELECT * FROM api_configs ORDER BY name ASC")
    fun getAll(): Flow<List<ApiConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ApiConfigEntity): Long

    @Update
    suspend fun update(config: ApiConfigEntity)

    @Delete
    suspend fun delete(config: ApiConfigEntity)

    @Query("DELETE FROM api_configs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM api_configs WHERE syncMode = 'periodic' AND isActive = 1")
    suspend fun getPeriodicConfigs(): List<ApiConfigEntity>

    @Query("SELECT * FROM api_configs WHERE id = :id")
    suspend fun getById(id: Long): ApiConfigEntity?
}
