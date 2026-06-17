package com.tagfile.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.dao.TrashDao
import com.tagfile.app.data.local.entity.BookEntity
import com.tagfile.app.data.local.entity.TagEntity
import com.tagfile.app.data.preferences.AppShortcut
import com.tagfile.app.data.preferences.AppearancePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalFiles: Long = 0,
    val totalTags: Int = 0,
    val totalBooks: Int = 0,
    val trashCount: Int = 0,
    val shortcuts: List<AppShortcut> = emptyList(),
    val showAddShortcutDialog: Boolean = false,
    val shortcutDialogType: String = "folder", // "folder", "tag", "book"
    val allTags: List<TagEntity> = emptyList(),
    val allBooks: List<BookEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fileIndexDao: FileIndexDao,
    private val tagDao: TagDao,
    private val bookDao: BookDao,
    private val trashDao: TrashDao,
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCounts()
        loadShortcuts()
    }

    private fun loadCounts() {
        viewModelScope.launch {
            try {
                val files = fileIndexDao.count()
                val tags = tagDao.getAllList().size
                val books = bookDao.count()
                val trash = trashDao.count()
                _uiState.update {
                    it.copy(
                        totalFiles = files,
                        totalTags = tags,
                        totalBooks = books,
                        trashCount = trash,
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadShortcuts() {
        viewModelScope.launch {
            appearancePreferences.shortcuts.collect { shortcuts ->
                _uiState.update { it.copy(shortcuts = shortcuts) }
            }
        }
    }

    fun addShortcut(shortcut: AppShortcut) {
        appearancePreferences.addShortcut(shortcut)
    }

    fun removeShortcut(index: Int) {
        appearancePreferences.removeShortcut(index)
    }

    fun showAddShortcutDialog() {
        _uiState.update { it.copy(showAddShortcutDialog = true, shortcutDialogType = "folder") }
    }

    fun dismissAddShortcutDialog() {
        _uiState.update { it.copy(showAddShortcutDialog = false) }
    }

    fun setShortcutDialogType(type: String) {
        _uiState.update { it.copy(shortcutDialogType = type) }
        if (type == "tag") {
            loadTagsForDialog()
        } else if (type == "book") {
            loadBooksForDialog()
        }
    }

    private fun loadTagsForDialog() {
        viewModelScope.launch {
            val tags = tagDao.getAllList()
            _uiState.update { it.copy(allTags = tags) }
        }
    }

    private fun loadBooksForDialog() {
        viewModelScope.launch {
            val books = bookDao.getAllList()
            _uiState.update { it.copy(allBooks = books) }
        }
    }
}