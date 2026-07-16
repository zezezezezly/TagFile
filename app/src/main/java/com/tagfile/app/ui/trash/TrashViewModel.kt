package com.tagfile.app.ui.trash

import android.util.Log
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
            originalFile.parentFile?.mkdirs()
            if (trashFile.renameTo(originalFile)) {
                trashDao.deleteById(id)
            } else {
                Log.e("TrashViewModel", "Failed to restore file: ${item.originalPath}")
            }
        }
    }

    fun permanentDelete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = trashDao.getById(id) ?: return@launch
            if (File(item.trashPath).deleteRecursively()) {
                trashDao.deleteById(id)
            } else {
                Log.e("TrashViewModel", "Failed to permanently delete: ${item.trashPath}")
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            tailrec fun deleteAll(files: List<java.io.File>): Boolean {
                if (files.isEmpty()) return true
                val file = files.first()
                val ok = if (file.isDirectory) file.deleteRecursively() else file.delete()
                return ok && deleteAll(files.drop(1))
            }
            val trashFiles = trashDao.getAllList().map { File(it.trashPath) }
            val success = deleteAll(trashFiles)
            if (success) {
                trashDao.deleteAll()
            } else {
                Log.e("TrashViewModel", "Failed to empty some trash files")
            }
        }
    }
}

data class TrashUiState(
    val items: List<TrashEntity> = emptyList(),
    val isLoading: Boolean = true
)
