package com.tagfile.app.ui.taggedfiles

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.Tag

data class TaggedFilesUiState(
    val tag: Tag? = null,
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
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

sealed class TaggedFilesEvent {
    data class LoadTag(val tagId: Long) : TaggedFilesEvent()
    object NavigateBack : TaggedFilesEvent()
    object ToggleSelectionMode : TaggedFilesEvent()
    data class ToggleFileSelection(val path: String) : TaggedFilesEvent()
    object ClearSelection : TaggedFilesEvent()
    object ShowTagSelector : TaggedFilesEvent()
    object HideTagSelector : TaggedFilesEvent()
    data class AddTagToSelectedFiles(val tagId: Long) : TaggedFilesEvent()
    object ShowRemoveTagSelector : TaggedFilesEvent()
    object HideRemoveTagSelector : TaggedFilesEvent()
    data class RemoveTagFromSelectedFiles(val tagId: Long) : TaggedFilesEvent()
    data class TagSelectorSearchQueryChanged(val query: String) : TaggedFilesEvent()
    data class RemoveTagSelectorSearchQueryChanged(val query: String) : TaggedFilesEvent()
    object ShowRenameDialog : TaggedFilesEvent()
    object HideRenameDialog : TaggedFilesEvent()
    data class RenameNameChanged(val name: String) : TaggedFilesEvent()
    object ConfirmRename : TaggedFilesEvent()
    object ShowDeleteConfirm : TaggedFilesEvent()
    object DismissDeleteConfirm : TaggedFilesEvent()
    object ConfirmDelete : TaggedFilesEvent()
    object ClearOperationMessage : TaggedFilesEvent()
}
