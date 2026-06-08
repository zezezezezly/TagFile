package com.tagfile.app.ui.typefiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.repository.SearchRepository
import com.tagfile.app.domain.repository.TagRepository
import com.tagfile.app.domain.usecase.FileOperationsUseCase
import com.tagfile.app.ui.common.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TypeFilesViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val tagRepository: TagRepository,
    private val fileOperationsUseCase: FileOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TypeFilesUiState())
    val uiState: StateFlow<TypeFilesUiState> = _uiState.asStateFlow()

    init {
        loadTags()
    }

    fun onEvent(event: TypeFilesEvent) {
        when (event) {
            is TypeFilesEvent.Initialize -> initialize(event.fileType)
            is TypeFilesEvent.LoadFiles -> loadFiles()
            is TypeFilesEvent.ToggleGridView -> toggleGridView()
            is TypeFilesEvent.ShowSortSheet -> _uiState.update { it.copy(showSortSheet = true) }
            is TypeFilesEvent.HideSortSheet -> _uiState.update { it.copy(showSortSheet = false) }
            is TypeFilesEvent.SortChanged -> {
                _uiState.update { it.copy(currentSort = event.sort) }
                reSort()
            }
            is TypeFilesEvent.ToggleSortOrder -> {
                _uiState.update { it.copy(ascending = !it.ascending) }
                reSort()
            }
            is TypeFilesEvent.ToggleSelectionMode -> _uiState.update {
                it.copy(isSelectionMode = !it.isSelectionMode, selectedPaths = emptySet())
            }
            is TypeFilesEvent.ToggleFileSelection -> {
                _uiState.update { state ->
                    val newSelected = state.selectedPaths.toMutableSet()
                    if (event.path in newSelected) newSelected.remove(event.path)
                    else newSelected.add(event.path)
                    if (newSelected.isEmpty()) state.copy(selectedPaths = newSelected, isSelectionMode = false)
                    else state.copy(selectedPaths = newSelected)
                }
            }
            is TypeFilesEvent.ClearSelection -> _uiState.update {
                it.copy(selectedPaths = emptySet(), isSelectionMode = false)
            }
            is TypeFilesEvent.ShowTagSelector -> _uiState.update {
                it.copy(showTagSelector = true, tagSelectorSearchQuery = "")
            }
            is TypeFilesEvent.HideTagSelector -> _uiState.update { it.copy(showTagSelector = false) }
            is TypeFilesEvent.AddTagToSelectedFiles -> addTagToSelectedFiles(event.tagId)
            is TypeFilesEvent.ShowRemoveTagSelector -> _uiState.update {
                it.copy(showRemoveTagSelector = true, removeTagSelectorSearchQuery = "")
            }
            is TypeFilesEvent.HideRemoveTagSelector -> _uiState.update { it.copy(showRemoveTagSelector = false) }
            is TypeFilesEvent.RemoveTagFromSelectedFiles -> removeTagFromSelectedFiles(event.tagId)
            is TypeFilesEvent.TagSelectorSearchQueryChanged -> _uiState.update {
                it.copy(tagSelectorSearchQuery = event.query)
            }
            is TypeFilesEvent.RemoveTagSelectorSearchQueryChanged -> _uiState.update {
                it.copy(removeTagSelectorSearchQuery = event.query)
            }
            is TypeFilesEvent.ShowRenameDialog -> {
                val target = _uiState.value.selectedPaths.firstOrNull()
                if (target != null) {
                    val name = java.io.File(target).name
                    _uiState.update {
                        it.copy(showRenameDialog = true, renameTargetPath = target, renameNewName = name)
                    }
                }
            }
            is TypeFilesEvent.HideRenameDialog -> _uiState.update { it.copy(showRenameDialog = false) }
            is TypeFilesEvent.RenameNameChanged -> _uiState.update { it.copy(renameNewName = event.name) }
            is TypeFilesEvent.ConfirmRename -> confirmRename()
            is TypeFilesEvent.ShowDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = true) }
            is TypeFilesEvent.DismissDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = false) }
            is TypeFilesEvent.ConfirmDelete -> confirmDelete()
            is TypeFilesEvent.ClearOperationMessage -> _uiState.update { it.copy(operationMessage = null) }
        }
    }

    fun initialize(fileType: FileType) {
        _uiState.update {
            it.copy(
                fileType = fileType,
                typeLabel = fileType.label,
                isLoading = true
            )
        }
        loadFiles()
    }

    private fun loadFiles() {
        val state = _uiState.value
        viewModelScope.launch {
            searchRepository.searchByType(state.fileType).fold(
                onSuccess = { files ->
                    val filesWithTags = files.map { file ->
                        val tags = tagRepository.getTagsByFilePath(file.path)
                        file.copy(tags = tags)
                    }
                    _uiState.update {
                        it.copy(files = sortFiles(filesWithTags, state.currentSort, state.ascending), isLoading = false)
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(files = emptyList(), isLoading = false) }
                }
            )
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(allTags = tags) }
            }
        }
    }

    private fun toggleGridView() {
        _uiState.update { it.copy(isGridView = !it.isGridView) }
    }

    private fun reSort() {
        _uiState.update { state ->
            state.copy(files = sortFiles(state.files, state.currentSort, state.ascending))
        }
    }

    private fun sortFiles(files: List<FileItem>, sort: SortOption, ascending: Boolean): List<FileItem> {
        val sorted = files.sortedWith(
            compareBy<FileItem> {
                when (sort) {
                    SortOption.NAME -> it.name.lowercase()
                    SortOption.DATE -> it.lastModified.toString()
                    SortOption.SIZE -> it.size.toString()
                    SortOption.TYPE -> it.extension.lowercase()
                }
            }
        )
        return if (ascending) sorted else sorted.reversed()
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
                    loadFiles()
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
                    loadFiles()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "删除失败: ${e.message}") }
                }
        }
    }
}
