package io.github.clsty.joplinshortcut.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.clsty.joplinshortcut.data.db.AppDatabase
import io.github.clsty.joplinshortcut.data.db.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "joplin_shortcut_db"
        ).build()
    }

    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun provideNotebookDao(db: AppDatabase): NotebookDao = db.notebookDao()
    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideApiConfigDao(db: AppDatabase): ApiConfigDao = db.apiConfigDao()
    @Provides fun provideWidgetConfigDao(db: AppDatabase): WidgetConfigDao = db.widgetConfigDao()
}
