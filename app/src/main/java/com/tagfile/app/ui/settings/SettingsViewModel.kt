package com.tagfile.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.preferences.PreferencesManager
import com.tagfile.app.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val bookDao: BookDao,
    private val shelfRepository: ShelfRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkMode = preferencesManager.isDarkMode.value,
            isContinuousEnhance = preferencesManager.continuousEnhance.value,
            shelfFolderPath = preferencesManager.shelfFolderPath.value
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.shelfFolderPath.collect { path ->
                _uiState.update { it.copy(shelfFolderPath = path) }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleDarkMode -> {
                val newValue = !_uiState.value.isDarkMode
                _uiState.update { it.copy(isDarkMode = newValue) }
                preferencesManager.setDarkMode(newValue)
            }
            is SettingsEvent.ToggleContinuousEnhance -> {
                val newValue = !_uiState.value.isContinuousEnhance
                _uiState.update { it.copy(isContinuousEnhance = newValue) }
                preferencesManager.setContinuousEnhance(newValue)
            }
            is SettingsEvent.UpdateShelfFolderPath -> {
                _uiState.update { it.copy(shelfFolderPath = event.path) }
                preferencesManager.setShelfFolderPath(event.path)
            }
            is SettingsEvent.ClearMessage -> _uiState.update { it.copy(message = null) }
            is SettingsEvent.ResetShelfDatabase -> resetShelfDatabase()
            is SettingsEvent.ScanShelfBooks -> scanShelfBooks()
        }
    }

    @Suppress("unused")
    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    private fun resetShelfDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResettingShelf = true) }
            try {
                bookDao.deleteAll()
                _uiState.update { it.copy(isResettingShelf = false, message = "书架数据库已重置") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isResettingShelf = false, message = "重置失败: ${e.message}") }
            }
        }
    }

    private fun scanShelfBooks() {
        val path = _uiState.value.shelfFolderPath ?: return
        _uiState.update { it.copy(isScanningShelf = true) }
        viewModelScope.launch {
            try {
                val newBooks = shelfRepository.scanAndAddBooks(path)
                _uiState.update {
                    it.copy(isScanningShelf = false, message = "已添加 ${newBooks.size} 本新书")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isScanningShelf = false, message = "扫描失败: ${e.message}")
                }
            }
        }
    }
}