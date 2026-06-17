package com.tagfile.app.ui.untagged

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.domain.model.FileItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UntaggedFilesViewModel @Inject constructor(
    private val fileIndexDao: FileIndexDao,
    private val fileTagDao: FileTagDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UntaggedFilesUiState())
    val uiState: StateFlow<UntaggedFilesUiState> = _uiState.asStateFlow()

    init {
        loadUntaggedFiles()
    }

    private fun loadUntaggedFiles() {
        viewModelScope.launch {
            try {
                val taggedPaths = withContext(Dispatchers.IO) {
                    fileTagDao.getAllDistinctFilePaths().toSet()
                }
                val allFiles = withContext(Dispatchers.IO) {
                    fileIndexDao.getAllFilesByPath()
                }
                val untagged = allFiles.filter { it.path !in taggedPaths }.map { entity ->
                    FileItem(
                        name = entity.name,
                        path = entity.path,
                        isDirectory = entity.isDirectory,
                        extension = entity.extension,
                        size = entity.size,
                        lastModified = entity.lastModified
                    )
                }
                _uiState.value = _uiState.value.copy(files = untagged, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, message = "加载失败: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

data class UntaggedFilesUiState(
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
)