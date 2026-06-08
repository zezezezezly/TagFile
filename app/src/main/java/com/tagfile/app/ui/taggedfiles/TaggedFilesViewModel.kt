package com.tagfile.app.ui.taggedfiles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.repository.FileRepository
import com.tagfile.app.domain.repository.TagRepository
import com.tagfile.app.domain.usecase.FileOperationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaggedFilesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository,
    private val fileRepository: FileRepository,
    private val fileOperationsUseCase: FileOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaggedFilesUiState())
    val uiState: StateFlow<TaggedFilesUiState> = _uiState.asStateFlow()

    private var currentTagId: Long = 0L

    init {
        val tagId = savedStateHandle.get<Long>("tagId") ?: 0L
        currentTagId = tagId
        if (tagId > 0) {
            loadTaggedFiles(tagId)
        }
        loadTags()
    }

    fun onEvent(event: TaggedFilesEvent) {
        when (event) {
            is TaggedFilesEvent.LoadTag -> loadTaggedFiles(event.tagId)
            is TaggedFilesEvent.NavigateBack -> { }
            is TaggedFilesEvent.ToggleSelectionMode -> _uiState.update {
                it.copy(isSelectionMode = !it.isSelectionMode, selectedPaths = emptySet())
            }
            is TaggedFilesEvent.ToggleFileSelection -> {
                _uiState.update { state ->
                    val newSelected = state.selectedPaths.toMutableSet()
                    if (event.path in newSelected) newSelected.remove(event.path)
                    else newSelected.add(event.path)
                    if (newSelected.isEmpty()) state.copy(selectedPaths = newSelected, isSelectionMode = false)
                    else state.copy(selectedPaths = newSelected)
                }
            }
            is TaggedFilesEvent.ClearSelection -> _uiState.update {
                it.copy(selectedPaths = emptySet(), isSelectionMode = false)
            }
            is TaggedFilesEvent.ShowTagSelector -> _uiState.update {
                it.copy(showTagSelector = true, tagSelectorSearchQuery = "")
            }
            is TaggedFilesEvent.HideTagSelector -> _uiState.update { it.copy(showTagSelector = false) }
            is TaggedFilesEvent.AddTagToSelectedFiles -> addTagToSelectedFiles(event.tagId)
            is TaggedFilesEvent.ShowRemoveTagSelector -> _uiState.update {
                it.copy(showRemoveTagSelector = true, removeTagSelectorSearchQuery = "")
            }
            is TaggedFilesEvent.HideRemoveTagSelector -> _uiState.update { it.copy(showRemoveTagSelector = false) }
            is TaggedFilesEvent.RemoveTagFromSelectedFiles -> removeTagFromSelectedFiles(event.tagId)
            is TaggedFilesEvent.TagSelectorSearchQueryChanged -> _uiState.update {
                it.copy(tagSelectorSearchQuery = event.query)
            }
            is TaggedFilesEvent.RemoveTagSelectorSearchQueryChanged -> _uiState.update {
                it.copy(removeTagSelectorSearchQuery = event.query)
            }
            is TaggedFilesEvent.ShowRenameDialog -> {
                val target = _uiState.value.selectedPaths.firstOrNull()
                if (target != null) {
                    val name = java.io.File(target).name
                    _uiState.update {
                        it.copy(showRenameDialog = true, renameTargetPath = target, renameNewName = name)
                    }
                }
            }
            is TaggedFilesEvent.HideRenameDialog -> _uiState.update { it.copy(showRenameDialog = false) }
            is TaggedFilesEvent.RenameNameChanged -> _uiState.update { it.copy(renameNewName = event.name) }
            is TaggedFilesEvent.ConfirmRename -> confirmRename()
            is TaggedFilesEvent.ShowDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = true) }
            is TaggedFilesEvent.DismissDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = false) }
            is TaggedFilesEvent.ConfirmDelete -> confirmDelete()
            is TaggedFilesEvent.ClearOperationMessage -> _uiState.update { it.copy(operationMessage = null) }
        }
    }

    private fun loadTaggedFiles(tagId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val tag = tagRepository.getTagById(tagId)
                val filePaths = tagRepository.getFilePathsByTagId(tagId)
                val files = filePaths.mapNotNull { path ->
                    fileRepository.getFileInfo(path).getOrNull()
                }.map { file ->
                    val tags = tagRepository.getTagsByFilePath(file.path)
                    file.copy(tags = tags)
                }

                _uiState.update {
                    it.copy(
                        tag = tag,
                        files = files,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(allTags = tags) }
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
            if (currentTagId > 0) {
                loadTaggedFiles(currentTagId)
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
                    if (currentTagId > 0) {
                        loadTaggedFiles(currentTagId)
                    }
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
                    if (currentTagId > 0) {
                        loadTaggedFiles(currentTagId)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "删除失败: ${e.message}") }
                }
        }
    }
}
