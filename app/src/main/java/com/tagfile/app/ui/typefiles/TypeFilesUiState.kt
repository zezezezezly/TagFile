package com.tagfile.app.ui.typefiles

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.ui.common.SortOption

data class TypeFilesUiState(
    val fileType: FileType = FileType.IMAGE,
    val typeLabel: String = "图片",
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val currentSort: SortOption = SortOption.NAME,
    val ascending: Boolean = true,
    val isGridView: Boolean = false,
    val showSortSheet: Boolean = false,
    val isSelectionMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
    val showTagSelector: Boolean = false,
    val allTags: List<Tag> = emptyList(),
    val tagSelectorSearchQuery: String = "",
    val showRemoveTagSelector: Boolean = false,
    val removeTagSelectorSearchQuery: String = "",
    val showRenameDialog: Boolean = false,
    val renameTargetPath: String? = null,
    val renameNewName: String = "",
    val showDeleteConfirm: Boolean = false,
    val operationMessage: String? = null,
    val isProcessing: Boolean = false
) {
    val filteredSelectorTags: List<Tag>
        get() = if (tagSelectorSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(tagSelectorSearchQuery, ignoreCase = true) }

    val filteredRemoveSelectorTags: List<Tag>
        get() = if (removeTagSelectorSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(removeTagSelectorSearchQuery, ignoreCase = true) }
}

sealed class TypeFilesEvent {
    data class Initialize(val fileType: FileType) : TypeFilesEvent()
    object LoadFiles : TypeFilesEvent()
    object ToggleGridView : TypeFilesEvent()
    object ShowSortSheet : TypeFilesEvent()
    object HideSortSheet : TypeFilesEvent()
    data class SortChanged(val sort: SortOption) : TypeFilesEvent()
    object ToggleSortOrder : TypeFilesEvent()
    object ToggleSelectionMode : TypeFilesEvent()
    data class ToggleFileSelection(val path: String) : TypeFilesEvent()
    object ClearSelection : TypeFilesEvent()
    object ShowTagSelector : TypeFilesEvent()
    object HideTagSelector : TypeFilesEvent()
    data class AddTagToSelectedFiles(val tagId: Long) : TypeFilesEvent()
    object ShowRemoveTagSelector : TypeFilesEvent()
    object HideRemoveTagSelector : TypeFilesEvent()
    data class RemoveTagFromSelectedFiles(val tagId: Long) : TypeFilesEvent()
    data class TagSelectorSearchQueryChanged(val query: String) : TypeFilesEvent()
    data class RemoveTagSelectorSearchQueryChanged(val query: String) : TypeFilesEvent()
    object ShowRenameDialog : TypeFilesEvent()
    object HideRenameDialog : TypeFilesEvent()
    data class RenameNameChanged(val name: String) : TypeFilesEvent()
    object ConfirmRename : TypeFilesEvent()
    object ShowDeleteConfirm : TypeFilesEvent()
    object DismissDeleteConfirm : TypeFilesEvent()
    object ConfirmDelete : TypeFilesEvent()
    object ClearOperationMessage : TypeFilesEvent()
}
