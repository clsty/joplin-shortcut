package io.github.clsty.joplinshortcut.ui.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.clsty.joplinshortcut.data.db.dao.FavoriteDao
import io.github.clsty.joplinshortcut.data.db.dao.NotebookDao
import io.github.clsty.joplinshortcut.data.db.dao.WidgetConfigDao
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteEntity
import io.github.clsty.joplinshortcut.data.db.entities.NotebookEntity
import io.github.clsty.joplinshortcut.data.db.entities.WidgetConfigEntity
import io.github.clsty.joplinshortcut.shortcut.ShortcutHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetManagementViewModel @Inject constructor(
    private val widgetConfigDao: WidgetConfigDao,
    private val notebookDao: NotebookDao,
    private val favoriteDao: FavoriteDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val widgetConfigs: StateFlow<List<WidgetConfigEntity>> = widgetConfigDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notebooks: StateFlow<List<NotebookEntity>> = notebookDao.getAllVisible()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = favoriteDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch { widgetConfigDao.insert(config) }
    }

    fun deleteWidgetConfig(config: WidgetConfigEntity) {
        viewModelScope.launch { widgetConfigDao.delete(config) }
    }

    fun createShortcut(id: String, title: String, type: String) {
        ShortcutHelper.createShortcut(context, "joplin_${type}_$id", title, id, type)
    }
}
