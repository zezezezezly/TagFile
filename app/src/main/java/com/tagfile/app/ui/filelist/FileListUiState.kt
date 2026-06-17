package com.tagfile.app.ui.filelist

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.ui.common.SortOption

data class FileListUiState(
    val currentPath: String = "",
    val isAtRoot: Boolean = true,
    val entryPath: String? = null,
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentSort: SortOption = SortOption.NAME,
    val ascending: Boolean = true,
    val isGridView: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val selectedFilePaths: Set<String> = emptySet(),
    val showHiddenFiles: Boolean = false,
    val showSortSheet: Boolean = false,
    val showNewFolderDialog: Boolean = false,
    val newFolderName: String = "",
    val hasStoragePermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val showTagSelector: Boolean = false,
    val showRenameDialog: Boolean = false,
    val renameTargetPath: String? = null,
    val renameNewName: String = "",
    val showDeleteConfirm: Boolean = false,
    val showOperationsMenu: Boolean = false,
    val allTags: List<Tag> = emptyList(),
    val operationMessage: String? = null,
    val isProcessing: Boolean = false,
    val tagSelectorSearchQuery: String = "",
    val showRemoveTagSelector: Boolean = false,
    val removeTagSelectorSearchQuery: String = "",
    val scrollToIndex: Int = 0,
    val scrollToOffset: Int = 0,
    val scrollRestoreKey: Long = 0L
) {
    val filteredSelectorTags: List<Tag>
        get() = if (tagSelectorSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(tagSelectorSearchQuery, ignoreCase = true) }

    val filteredRemoveSelectorTags: List<Tag>
        get() = if (removeTagSelectorSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(removeTagSelectorSearchQuery, ignoreCase = true) }
}

data class ScrollPosition(val index: Int, val offset: Int)

sealed class FileListEvent {
    data class NavigateTo(val path: String, val scrollIndex: Int, val scrollOffset: Int) : FileListEvent()
    data class NavigateUp(val currentPath: String, val scrollIndex: Int, val scrollOffset: Int) : FileListEvent()
    data class SortChanged(val option: SortOption) : FileListEvent()
    object ToggleSortOrder : FileListEvent()
    object ToggleGridView : FileListEvent()
    object ToggleShowHiddenFiles : FileListEvent()
    object ToggleSelectionMode : FileListEvent()
    data class ToggleFileSelection(val path: String) : FileListEvent()
    object ClearSelection : FileListEvent()
    object ShowSortSheet : FileListEvent()
    object HideSortSheet : FileListEvent()
    object ShowNewFolderDialog : FileListEvent()
    object HideNewFolderDialog : FileListEvent()
    data class NewFolderNameChanged(val name: String) : FileListEvent()
    object CreateNewFolder : FileListEvent()
    object RequestPermission : FileListEvent()
    object DismissPermissionDialog : FileListEvent()
    object GoToSettings : FileListEvent()
    object ShowTagSelector : FileListEvent()
    object HideTagSelector : FileListEvent()
    data class AddTagToSelectedFiles(val tagId: Long) : FileListEvent()
    object ShowRenameDialog : FileListEvent()
    object HideRenameDialog : FileListEvent()
    data class RenameNameChanged(val name: String) : FileListEvent()
    object ConfirmRename : FileListEvent()
    object ShowDeleteConfirm : FileListEvent()
    object DismissDeleteConfirm : FileListEvent()
    object ConfirmDelete : FileListEvent()
    object ShowOperationsMenu : FileListEvent()
    object HideOperationsMenu : FileListEvent()
    data class CopySelectedTo(val destinationDir: String) : FileListEvent()
    data class MoveSelectedTo(val destinationDir: String) : FileListEvent()
    object ClearOperationMessage : FileListEvent()
    data class TagSelectorSearchQueryChanged(val query: String) : FileListEvent()
    object ShowRemoveTagSelector : FileListEvent()
    object HideRemoveTagSelector : FileListEvent()
    data class RemoveTagSelectorSearchQueryChanged(val query: String) : FileListEvent()
    data class RemoveTagFromSelectedFiles(val tagId: Long) : FileListEvent()
    object ToggleSelectAll : FileListEvent()
    object ToggleMultiSelectMode : FileListEvent()
    object SelectAllFiles : FileListEvent()
    object BatchDeleteFiles : FileListEvent()
    data class BatchTagFiles(val tagId: Long) : FileListEvent()
    data class CopyFileTo(val sourcePath: String, val targetDir: String) : FileListEvent()
    data class MoveFileTo(val sourcePath: String, val targetDir: String) : FileListEvent()
}
