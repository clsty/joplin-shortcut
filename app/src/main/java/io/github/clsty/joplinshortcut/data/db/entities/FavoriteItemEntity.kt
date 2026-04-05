package io.github.clsty.joplinshortcut.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_items")
data class FavoriteItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val favoriteId: Long,
    val itemId: String,
    val itemType: String, // "note" or "notebook"
    val title: String
)
