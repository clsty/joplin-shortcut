package io.github.clsty.joplinshortcut.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val parentId: String,
    val updatedTime: Long = 0L,
    val isHidden: Boolean = false
)
