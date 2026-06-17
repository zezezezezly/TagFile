package com.tagfile.app.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.filesystem.FileIndexer
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.repository.SearchRepository
import com.tagfile.app.domain.repository.TagRepository
import com.tagfile.app.domain.usecase.FileOperationsUseCase
import com.tagfile.app.domain.usecase.SearchFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val searchFilesUseCase: SearchFilesUseCase,
    private val tagRepository: TagRepository,
    private val fileOperationsUseCase: FileOperationsUseCase,
    private val searchRepository: SearchRepository,
    private val fileIndexer: FileIndexer
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        loadRecentFiles()
        loadTags()
        observeIndexingProgress()
    }

    fun onEvent(event: CategoryEvent) {
        when (event) {
            is CategoryEvent.LoadCategories -> loadCategories()
            is CategoryEvent.LoadRecentFiles -> loadRecentFiles()
            is CategoryEvent.LoadLargeFiles -> loadLargeFiles()
            is CategoryEvent.ToggleLargeFiles -> {
                _uiState.update { it.copy(showLargeFiles = !it.showLargeFiles) }
                if (_uiState.value.largeFiles.isEmpty()) {
                    loadLargeFiles()
                }
            }
            is CategoryEvent.ToggleSelectionMode -> _uiState.update {
                it.copy(isSelectionMode = !it.isSelectionMode, selectedPaths = emptySet())
            }
            is CategoryEvent.ToggleFileSelection -> {
                _uiState.update { state ->
                    val newSelected = state.selectedPaths.toMutableSet()
                    if (event.path in newSelected) newSelected.remove(event.path)
                    else newSelected.add(event.path)
                    if (newSelected.isEmpty()) state.copy(selectedPaths = newSelected, isSelectionMode = false)
                    else state.copy(selectedPaths = newSelected)
                }
            }
            is CategoryEvent.ClearSelection -> _uiState.update {
                it.copy(selectedPaths = emptySet(), isSelectionMode = false)
            }
            is CategoryEvent.ShowTagSelector -> _uiState.update {
                it.copy(showTagSelector = true, tagSelectorSearchQuery = "")
            }
            is CategoryEvent.HideTagSelector -> _uiState.update { it.copy(showTagSelector = false) }
            is CategoryEvent.AddTagToSelectedFiles -> addTagToSelectedFiles(event.tagId)
            is CategoryEvent.ShowRemoveTagSelector -> _uiState.update {
                it.copy(showRemoveTagSelector = true, removeTagSelectorSearchQuery = "")
            }
            is CategoryEvent.HideRemoveTagSelector -> _uiState.update { it.copy(showRemoveTagSelector = false) }
            is CategoryEvent.RemoveTagFromSelectedFiles -> removeTagFromSelectedFiles(event.tagId)
            is CategoryEvent.TagSelectorSearchQueryChanged -> _uiState.update {
                it.copy(tagSelectorSearchQuery = event.query)
            }
            is CategoryEvent.RemoveTagSelectorSearchQueryChanged -> _uiState.update {
                it.copy(removeTagSelectorSearchQuery = event.query)
            }
            is CategoryEvent.ShowRenameDialog -> {
                val target = _uiState.value.selectedPaths.firstOrNull()
                if (target != null) {
                    val name = java.io.File(target).name
                    _uiState.update {
                        it.copy(showRenameDialog = true, renameTargetPath = target, renameNewName = name)
                    }
                }
            }
            is CategoryEvent.HideRenameDialog -> _uiState.update { it.copy(showRenameDialog = false) }
            is CategoryEvent.RenameNameChanged -> _uiState.update { it.copy(renameNewName = event.name) }
            is CategoryEvent.ConfirmRename -> confirmRename()
            is CategoryEvent.ShowDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = true) }
            is CategoryEvent.DismissDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = false) }
            is CategoryEvent.ConfirmDelete -> confirmDelete()
            is CategoryEvent.ClearOperationMessage -> _uiState.update { it.copy(operationMessage = null) }
        }
    }

    private fun loadCategories() {
        _uiState.update { it.copy(isLoading = true) }
        refreshCategoryCounts()
    }

    private fun refreshCategoryCounts() {
        viewModelScope.launch {
            val typeCounts = mutableMapOf<String, Long>()
            val types = FileType.entries.filter { it != FileType.OTHER }

            types.forEach { fileType ->
                val count = searchRepository.getFileCountByExtensions(fileType.extensions)
                typeCounts[fileType.label] = count
            }

            val untaggedCount = searchRepository.getUntaggedFileCount()

            _uiState.update { it.copy(typeResults = typeCounts, untaggedCount = untaggedCount, isLoading = false) }
        }
    }

    private fun observeIndexingProgress() {
        viewModelScope.launch {
            var lastCount = 0L
            fileIndexer.indexedCount.collect { count ->
                if (count > lastCount) {
                    lastCount = count
                    refreshCategoryCounts()
                }
            }
        }
    }

    private fun loadRecentFiles() {
        viewModelScope.launch {
            searchFilesUseCase.getRecentFiles(30).onSuccess { files ->
                _uiState.update { it.copy(recentFiles = files) }
            }
        }
    }

    private fun loadLargeFiles() {
        viewModelScope.launch {
            searchFilesUseCase.getLargeFiles().onSuccess { files ->
                _uiState.update { it.copy(largeFiles = files) }
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(allTags = tags, tags = tags.take(10)) }
            }
        }
    }

    private fun addTagToSelectedFiles(tagId: Long) {
        viewModelScope.launch {
            val paths = _uiState.value.selectedPaths.toList()
            paths.forEach { path ->
                tagRepository.addTagToFile(path, tagId)
            }
            _uiState.update {
                it.copy(
                    showTagSelector = false,
                    isSelectionMode = false,
                    selectedPaths = emptySet(),
                    operationMessage = "已为 ${paths.size} 个项目添加标签"
                )
            }
        }
    }

    private fun removeTagFromSelectedFiles(tagId: Long) {
        viewModelScope.launch {
            val paths = _uiState.value.selectedPaths.toList()
            paths.forEach { path ->
                tagRepository.removeTagFromFile(path, tagId)
            }
            _uiState.update {
                it.copy(
                    showRemoveTagSelector = false,
                    isSelectionMode = false,
                    selectedPaths = emptySet(),
                    operationMessage = "已为 ${paths.size} 个项目移除标签"
                )
            }
        }
    }

    private fun confirmRename() {
        val target = _uiState.value.renameTargetPath ?: return
        val newName = _uiState.value.renameNewName.trim()
        if (newName.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            fileOperationsUseCase.renameFile(target, newName)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showRenameDialog = false, isProcessing = false,
                            selectedPaths = emptySet(), isSelectionMode = false,
                            operationMessage = "重命名成功"
                        )
                    }
                    loadRecentFiles()
                    loadLargeFiles()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "重命名失败: ${e.message}") }
                }
        }
    }

    private fun confirmDelete() {
        val paths = _uiState.value.selectedPaths.toList()
        if (paths.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            fileOperationsUseCase.deleteFiles(paths)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showDeleteConfirm = false, isProcessing = false,
                            selectedPaths = emptySet(), isSelectionMode = false,
                            operationMessage = "已删除 ${paths.size} 个文件"
                        )
                    }
                    loadRecentFiles()
                    loadLargeFiles()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "删除失败: ${e.message}") }
                }
        }
    }
}
