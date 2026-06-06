package com.tagfile.app.ui.category

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.Tag

data class CategoryUiState(
    val isLoading: Boolean = false,
    val typeResults: Map<String, Long> = emptyMap(),
    val tags: List<Tag> = emptyList(),
    val recentFiles: List<FileItem> = emptyList(),
    val largeFiles: List<FileItem> = emptyList(),
    val showLargeFiles: Boolean = false,
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

sealed class CategoryEvent {
    object LoadCategories : CategoryEvent()
    object LoadRecentFiles : CategoryEvent()
    object LoadLargeFiles : CategoryEvent()
    object ToggleLargeFiles : CategoryEvent()
    object ToggleSelectionMode : CategoryEvent()
    data class ToggleFileSelection(val path: String) : CategoryEvent()
    object ClearSelection : CategoryEvent()
    object ShowTagSelector : CategoryEvent()
    object HideTagSelector : CategoryEvent()
    data class AddTagToSelectedFiles(val tagId: Long) : CategoryEvent()
    object ShowRemoveTagSelector : CategoryEvent()
    object HideRemoveTagSelector : CategoryEvent()
    data class RemoveTagFromSelectedFiles(val tagId: Long) : CategoryEvent()
    data class TagSelectorSearchQueryChanged(val query: String) : CategoryEvent()
    data class RemoveTagSelectorSearchQueryChanged(val query: String) : CategoryEvent()
    object ShowRenameDialog : CategoryEvent()
    object HideRenameDialog : CategoryEvent()
    data class RenameNameChanged(val name: String) : CategoryEvent()
    object ConfirmRename : CategoryEvent()
    object ShowDeleteConfirm : CategoryEvent()
    object DismissDeleteConfirm : CategoryEvent()
    object ConfirmDelete : CategoryEvent()
    object ClearOperationMessage : CategoryEvent()
}
