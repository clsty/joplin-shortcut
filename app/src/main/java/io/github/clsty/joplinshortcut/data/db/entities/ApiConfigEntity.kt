package io.github.clsty.joplinshortcut.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_configs")
data class ApiConfigEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val baseUrl: String,
    val token: String = "",
    val syncMode: String = "manual", // "manual" or "periodic"
    val syncIntervalMinutes: Int = 60,
    val isActive: Boolean = true
)
