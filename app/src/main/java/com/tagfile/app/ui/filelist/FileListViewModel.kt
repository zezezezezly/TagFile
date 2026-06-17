package com.tagfile.app.ui.filelist

import android.content.Context
import android.os.Environment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.local.dao.TrashDao
import com.tagfile.app.data.local.entity.TrashEntity
import com.tagfile.app.domain.repository.TagRepository
import com.tagfile.app.domain.usecase.BrowseFilesUseCase
import com.tagfile.app.domain.usecase.FileOperationsUseCase
import com.tagfile.app.ui.common.SortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val browseFilesUseCase: BrowseFilesUseCase,
    private val fileOperationsUseCase: FileOperationsUseCase,
    private val tagRepository: TagRepository,
    private val trashDao: TrashDao,
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileListUiState())
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()

    private val scrollPositionCache = mutableMapOf<String, ScrollPosition>()

    private val rootPath: String =
        Environment.getExternalStorageDirectory()?.absolutePath ?: "/storage/emulated/0"

    init {
        val targetDir = savedStateHandle.get<String>("dir")
        val startPath = targetDir ?: rootPath
        _uiState.update { it.copy(entryPath = targetDir) }
        loadFiles(startPath)
        loadAllTags()
    }

    fun onEvent(event: FileListEvent) {
        when (event) {
            is FileListEvent.NavigateTo -> {
                scrollPositionCache[_uiState.value.currentPath] = ScrollPosition(event.scrollIndex, event.scrollOffset)
                loadFiles(event.path)
            }
            is FileListEvent.NavigateUp -> {
                scrollPositionCache[_uiState.value.currentPath] = ScrollPosition(event.scrollIndex, event.scrollOffset)
                navigateUp(event.currentPath)
            }
            is FileListEvent.SortChanged -> {
                _uiState.update { it.copy(currentSort = event.option) }
                reloadCurrentDirectory()
            }
            is FileListEvent.ToggleSortOrder -> {
                _uiState.update { it.copy(ascending = !it.ascending) }
                reloadCurrentDirectory()
            }
            is FileListEvent.ToggleGridView -> _uiState.update { it.copy(isGridView = !it.isGridView) }
            is FileListEvent.ToggleShowHiddenFiles -> {
                _uiState.update { it.copy(showHiddenFiles = !it.showHiddenFiles) }
                reloadCurrentDirectory()
            }
            is FileListEvent.ToggleSelectionMode -> _uiState.update {
                it.copy(isSelectionMode = !it.isSelectionMode, selectedPaths = emptySet(), showOperationsMenu = false)
            }
            is FileListEvent.ToggleFileSelection -> {
                _uiState.update { state ->
                    val newSelected = state.selectedPaths.toMutableSet()
                    if (event.path in newSelected) newSelected.remove(event.path)
                    else newSelected.add(event.path)
                    if (newSelected.isEmpty()) state.copy(selectedPaths = newSelected, isSelectionMode = false, showOperationsMenu = false)
                    else state.copy(selectedPaths = newSelected)
                }
            }
            is FileListEvent.ClearSelection -> _uiState.update { it.copy(selectedPaths = emptySet(), isSelectionMode = false, showOperationsMenu = false) }
            is FileListEvent.ShowSortSheet -> _uiState.update { it.copy(showSortSheet = true) }
            is FileListEvent.HideSortSheet -> _uiState.update { it.copy(showSortSheet = false) }
            is FileListEvent.ShowNewFolderDialog -> _uiState.update { it.copy(showNewFolderDialog = true, newFolderName = "") }
            is FileListEvent.HideNewFolderDialog -> _uiState.update { it.copy(showNewFolderDialog = false) }
            is FileListEvent.NewFolderNameChanged -> _uiState.update { it.copy(newFolderName = event.name) }
            is FileListEvent.CreateNewFolder -> createNewFolder()
            is FileListEvent.RequestPermission -> _uiState.update { it.copy(showPermissionDialog = true) }
            is FileListEvent.DismissPermissionDialog -> _uiState.update { it.copy(showPermissionDialog = false) }
            is FileListEvent.GoToSettings -> _uiState.update { it.copy(showPermissionDialog = false) }
            is FileListEvent.ShowTagSelector -> _uiState.update { it.copy(showTagSelector = true, tagSelectorSearchQuery = "") }
            is FileListEvent.HideTagSelector -> _uiState.update { it.copy(showTagSelector = false) }
            is FileListEvent.AddTagToSelectedFiles -> addTagToSelectedFiles(event.tagId)
            is FileListEvent.ShowRenameDialog -> {
                val target = _uiState.value.selectedPaths.firstOrNull()
                if (target != null) {
                    val name = File(target).name
                    _uiState.update { it.copy(showRenameDialog = true, renameTargetPath = target, renameNewName = name) }
                }
            }
            is FileListEvent.HideRenameDialog -> _uiState.update { it.copy(showRenameDialog = false) }
            is FileListEvent.RenameNameChanged -> _uiState.update { it.copy(renameNewName = event.name) }
            is FileListEvent.ConfirmRename -> confirmRename()
            is FileListEvent.ShowDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = true) }
            is FileListEvent.DismissDeleteConfirm -> _uiState.update { it.copy(showDeleteConfirm = false) }
            is FileListEvent.ConfirmDelete -> confirmDelete()
            is FileListEvent.ShowOperationsMenu -> _uiState.update { it.copy(showOperationsMenu = true) }
            is FileListEvent.HideOperationsMenu -> _uiState.update { it.copy(showOperationsMenu = false) }
            is FileListEvent.CopySelectedTo -> copySelectedTo(event.destinationDir)
            is FileListEvent.MoveSelectedTo -> moveSelectedTo(event.destinationDir)
            is FileListEvent.ClearOperationMessage -> _uiState.update { it.copy(operationMessage = null) }
            is FileListEvent.TagSelectorSearchQueryChanged -> _uiState.update { it.copy(tagSelectorSearchQuery = event.query) }
            is FileListEvent.ShowRemoveTagSelector -> _uiState.update { it.copy(showRemoveTagSelector = true, removeTagSelectorSearchQuery = "") }
            is FileListEvent.HideRemoveTagSelector -> _uiState.update { it.copy(showRemoveTagSelector = false) }
            is FileListEvent.RemoveTagSelectorSearchQueryChanged -> _uiState.update { it.copy(removeTagSelectorSearchQuery = event.query) }
            is FileListEvent.RemoveTagFromSelectedFiles -> removeTagFromSelectedFiles(event.tagId)
            is FileListEvent.ToggleSelectAll -> toggleSelectAll()
            is FileListEvent.ToggleMultiSelectMode -> _uiState.update {
                it.copy(isMultiSelectMode = !it.isMultiSelectMode, selectedFilePaths = emptySet())
            }
            is FileListEvent.SelectAllFiles -> {
                val allPaths = _uiState.value.files.map { it.path }.toSet()
                _uiState.update { it.copy(selectedFilePaths = allPaths) }
            }
            is FileListEvent.BatchDeleteFiles -> batchDeleteFiles()
            is FileListEvent.BatchTagFiles -> batchTagFiles(event.tagId)
            is FileListEvent.CopyFileTo -> copyFile(event.sourcePath, event.targetDir)
            is FileListEvent.MoveFileTo -> moveFile(event.sourcePath, event.targetDir)
        }
    }

    private fun loadFiles(path: String) {
        _uiState.update { it.copy(currentPath = path, isAtRoot = path == rootPath, isLoading = true, error = null) }
        viewModelScope.launch {
            browseFilesUseCase(path).fold(
                onSuccess = { files ->
                    val filtered = if (_uiState.value.showHiddenFiles) files
                        else files.filter { !it.name.startsWith(".") }
                    val sorted = sortFiles(filtered, _uiState.value.currentSort, _uiState.value.ascending)
                    val cached = scrollPositionCache[path]
                    _uiState.update {
                        it.copy(
                            files = sorted,
                            isLoading = false,
                            scrollToIndex = cached?.index ?: 0,
                            scrollToOffset = cached?.offset ?: 0,
                            scrollRestoreKey = it.scrollRestoreKey + 1
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            )
        }
    }

    private fun reloadCurrentDirectory() {
        loadFiles(_uiState.value.currentPath)
    }

    private fun navigateUp(currentPath: String) {
        if (currentPath == rootPath) return
        val parentFile = File(currentPath).parentFile
        if (parentFile != null) {
            loadFiles(parentFile.absolutePath)
        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(allTags = tags) }
            }
        }
    }

    private fun sortFiles(files: List<com.tagfile.app.domain.model.FileItem>, sort: SortOption, ascending: Boolean): List<com.tagfile.app.domain.model.FileItem> {
        val sorted = files.sortedWith(
            compareBy<com.tagfile.app.domain.model.FileItem> { !it.isDirectory }.thenBy {
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

    private fun createNewFolder() {
        val name = _uiState.value.newFolderName.trim()
        if (name.isEmpty()) return

        viewModelScope.launch {
            fileOperationsUseCase.createDirectory(_uiState.value.currentPath, name)
                .onSuccess { reloadCurrentDirectory() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
            _uiState.update { it.copy(showNewFolderDialog = false) }
        }
    }

    private fun addTagToSelectedFiles(tagId: Long) {
        viewModelScope.launch {
            val paths = _uiState.value.selectedPaths.toList()
            paths.forEach { path ->
                tagRepository.addTagToFile(path, tagId)
            }
            _uiState.update { it.copy(showTagSelector = false, operationMessage = "已为 ${paths.size} 个项目添加标签") }
            reloadCurrentDirectory()
        }
    }

    private fun removeTagFromSelectedFiles(tagId: Long) {
        viewModelScope.launch {
            val paths = _uiState.value.selectedPaths.toList()
            paths.forEach { path ->
                tagRepository.removeTagFromFile(path, tagId)
            }
            _uiState.update { it.copy(showRemoveTagSelector = false, operationMessage = "已为 ${paths.size} 个项目移除标签") }
            reloadCurrentDirectory()
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
                    _uiState.update { it.copy(showRenameDialog = false, isProcessing = false, operationMessage = "重命名成功") }
                    reloadCurrentDirectory()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "重命名失败: ${e.message}") }
                }
        }
    }

    private fun confirmDelete() {
        val paths = _uiState.value.selectedPaths.toList()
        if (paths.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val trashDir = File(context.filesDir, "trash")
                if (!trashDir.exists()) trashDir.mkdirs()

                paths.forEach { path ->
                    val file = File(path)
                    if (file.exists()) {
                        val trashFile = File(trashDir, "${System.currentTimeMillis()}_${file.name}")
                        if (file.renameTo(trashFile)) {
                            trashDao.insert(
                                TrashEntity(
                                    originalPath = path,
                                    trashPath = trashFile.absolutePath,
                                    fileName = file.name,
                                    isDirectory = file.isDirectory,
                                    fileSize = if (file.isDirectory) 0L else file.length()
                                )
                            )
                            tagRepository.deleteAllCrossRefsByFilePath(path)
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        showDeleteConfirm = false,
                        isProcessing = false,
                        selectedPaths = emptySet(),
                        isSelectionMode = false,
                        operationMessage = "已删除 ${paths.size} 个文件"
                    )
                }
                reloadCurrentDirectory()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, operationMessage = "删除失败: ${e.message}")
                }
            }
        }
    }

    private fun copySelectedTo(destinationDir: String) {
        val paths = _uiState.value.selectedPaths.toList()
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            fileOperationsUseCase.copyFiles(paths, destinationDir)
                .onSuccess {
                    _uiState.update { it.copy(isProcessing = false, selectedPaths = emptySet(), isSelectionMode = false, operationMessage = "已复制 ${paths.size} 个文件") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "复制失败: ${e.message}") }
                }
        }
    }

    private fun moveSelectedTo(destinationDir: String) {
        val paths = _uiState.value.selectedPaths.toList()
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            fileOperationsUseCase.moveFiles(paths, destinationDir)
                .onSuccess {
                    _uiState.update { it.copy(isProcessing = false, selectedPaths = emptySet(), isSelectionMode = false, operationMessage = "已移动 ${paths.size} 个文件") }
                    reloadCurrentDirectory()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "移动失败: ${e.message}") }
                }
        }
    }

    private fun toggleSelectAll() {
        val allPaths = _uiState.value.files.map { it.path }.toSet()
        _uiState.update { state ->
            if (state.selectedPaths.size == allPaths.size && allPaths.isNotEmpty()) {
                state.copy(selectedPaths = emptySet(), isSelectionMode = false)
            } else {
                state.copy(selectedPaths = allPaths, isSelectionMode = true)
            }
        }
    }

    private fun batchDeleteFiles() {
        val paths = _uiState.value.selectedFilePaths.toList()
        if (paths.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            fileOperationsUseCase.deleteFiles(paths)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            selectedFilePaths = emptySet(),
                            isMultiSelectMode = false,
                            operationMessage = "已删除 ${paths.size} 个文件"
                        )
                    }
                    reloadCurrentDirectory()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isProcessing = false, operationMessage = "删除失败: ${e.message}") }
                }
        }
    }

    private fun batchTagFiles(tagId: Long) {
        viewModelScope.launch {
            val paths = _uiState.value.selectedFilePaths.toList()
            paths.forEach { path ->
                tagRepository.addTagToFile(path, tagId)
            }
            _uiState.update {
                it.copy(
                    operationMessage = "已为 ${paths.size} 个项目添加标签",
                    showTagSelector = false
                )
            }
            reloadCurrentDirectory()
        }
    }

    fun copyFile(sourcePath: String, targetDir: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val sourceFile = File(sourcePath)
                val targetFile = File(targetDir, sourceFile.name)
                if (sourceFile.isDirectory) {
                    sourceFile.copyRecursively(targetFile, overwrite = false)
                } else {
                    sourceFile.copyTo(targetFile, overwrite = false)
                }
                _uiState.update {
                    it.copy(isProcessing = false, operationMessage = "已复制: ${sourceFile.name}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, operationMessage = "复制失败: ${e.message}")
                }
            }
        }
    }

    fun moveFile(sourcePath: String, targetDir: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            try {
                val sourceFile = File(sourcePath)
                val targetFile = File(targetDir, sourceFile.name)
                val success = if (sourceFile.isDirectory) {
                    sourceFile.renameTo(targetFile)
                } else {
                    sourceFile.renameTo(targetFile)
                }
                if (success) {
                    _uiState.update {
                        it.copy(isProcessing = false, operationMessage = "已移动: ${sourceFile.name}")
                    }
                    reloadCurrentDirectory()
                } else {
                    _uiState.update {
                        it.copy(isProcessing = false, operationMessage = "移动失败")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, operationMessage = "移动失败: ${e.message}")
                }
            }
        }
    }
}
