package io.github.clsty.joplinshortcut.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.clsty.joplinshortcut.data.db.dao.*
import io.github.clsty.joplinshortcut.data.db.entities.*

@Database(
    entities = [
        NoteEntity::class,
        NotebookEntity::class,
        FavoriteEntity::class,
        FavoriteItemEntity::class,
        ApiConfigEntity::class,
        WidgetConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun notebookDao(): NotebookDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun apiConfigDao(): ApiConfigDao
    abstract fun widgetConfigDao(): WidgetConfigDao
}
