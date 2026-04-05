package io.github.clsty.joplinshortcut.data.db.dao

import androidx.room.*
import io.github.clsty.joplinshortcut.data.db.entities.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_configs ORDER BY name ASC")
    fun getAll(): Flow<List<WidgetConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: WidgetConfigEntity): Long

    @Update
    suspend fun update(config: WidgetConfigEntity)

    @Delete
    suspend fun delete(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM widget_configs WHERE widgetId = :widgetId LIMIT 1")
    suspend fun getByWidgetId(widgetId: Int): WidgetConfigEntity?

    @Query("DELETE FROM widget_configs WHERE widgetId = :widgetId")
    suspend fun deleteByWidgetId(widgetId: Int)
}
