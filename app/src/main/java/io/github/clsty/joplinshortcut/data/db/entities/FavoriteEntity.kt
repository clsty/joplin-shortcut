package io.github.clsty.joplinshortcut.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites", indices = [Index(value = ["name"], unique = true)])
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
