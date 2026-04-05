package io.github.clsty.joplinshortcut.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val widgetId: Int,
    val name: String,
    val sourceType: String, // "notebook" or "favorite"
    val sourceId: String,
    val recursiveDepth: Int = 1
)
