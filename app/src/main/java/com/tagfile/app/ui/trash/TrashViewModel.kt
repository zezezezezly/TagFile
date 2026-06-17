package com.tagfile.app.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.local.dao.TrashDao
import com.tagfile.app.data.local.entity.TrashEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val trashDao: TrashDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            trashDao.getAll().collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
        // Auto-clean items older than 30 days
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            trashDao.deleteOlderThan(cutoff)
        }
    }

    fun restore(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = trashDao.getById(id) ?: return@launch
            val trashFile = File(item.trashPath)
            val originalFile = File(item.originalPath)
            // Ensure parent directory exists
            originalFile.parentFile?.mkdirs()
            trashFile.renameTo(originalFile)
            trashDao.deleteById(id)
        }
    }

    fun permanentDelete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = trashDao.getById(id) ?: return@launch
            File(item.trashPath).deleteRecursively()
            trashDao.deleteById(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            trashDao.getAllList().forEach { File(it.trashPath).deleteRecursively() }
            trashDao.deleteAll()
        }
    }
}

data class TrashUiState(
    val items: List<TrashEntity> = emptyList(),
    val isLoading: Boolean = true
)