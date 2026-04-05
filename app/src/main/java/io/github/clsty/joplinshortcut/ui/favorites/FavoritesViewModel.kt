package io.github.clsty.joplinshortcut.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.clsty.joplinshortcut.data.db.dao.FavoriteDao
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteEntity
import io.github.clsty.joplinshortcut.data.db.entities.FavoriteItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteDao: FavoriteDao
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = favoriteDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedFavoriteId = MutableStateFlow<Long?>(null)

    val selectedFavoriteItems: StateFlow<List<FavoriteItemEntity>> = selectedFavoriteId
        .flatMapLatest { id ->
            if (id != null) favoriteDao.getItems(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFavorite(id: Long?) {
        selectedFavoriteId.value = id
    }

    fun createFavorite(name: String) {
        viewModelScope.launch {
            runCatching { favoriteDao.insert(FavoriteEntity(name = name)) }
        }
    }

    fun deleteFavorite(favorite: FavoriteEntity) {
        viewModelScope.launch {
            favoriteDao.clearItems(favorite.id)
            favoriteDao.delete(favorite)
        }
    }

    fun removeItem(favoriteId: Long, itemId: String) {
        viewModelScope.launch {
            favoriteDao.removeItem(favoriteId, itemId)
        }
    }
}
