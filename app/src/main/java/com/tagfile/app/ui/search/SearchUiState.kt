package com.tagfile.app.ui.search

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.domain.model.TagMode

data class SearchUiState(
    val query: String = "",
    val results: List<FileItem> = emptyList(),
    val isSearching: Boolean = false,
    val allTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val tagMode: TagMode = TagMode.AND,
    val selectedFileTypes: Set<FileType> = emptySet(),
    val searchDirectories: Boolean = false,
    val searchHistory: List<String> = emptyList(),
    val isSelectionMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
    val showTagSelector: Boolean = false,
    val operationMessage: String? = null,
    val tagSearchQuery: String = "",
    val showTagPicker: Boolean = false,
    val tagPickerSearchQuery: String = "",
    val pickerSelectedTagIds: Set<Long> = emptySet(),
    val tagSelectorSearchQuery: String = "",
    val showRemoveTagSelector: Boolean = false,
    val removeTagSelectorSearchQuery: String = ""
) {
    val filteredTags: List<Tag>
        get() = if (tagSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(tagSearchQuery, ignoreCase = true) }

    val filteredPickerTags: List<Tag>
        get() = if (tagPickerSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(tagPickerSearchQuery, ignoreCase = true) }

    val filteredSelectorTags: List<Tag>
        get() = if (tagSelectorSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(tagSelectorSearchQuery, ignoreCase = true) }

    val filteredRemoveSelectorTags: List<Tag>
        get() = if (removeTagSelectorSearchQuery.isBlank()) allTags
        else allTags.filter { it.name.contains(removeTagSelectorSearchQuery, ignoreCase = true) }
}

sealed class SearchEvent {
    data class QueryChanged(val query: String) : SearchEvent()
    object ClearQuery : SearchEvent()
    object PerformSearch : SearchEvent()
    data class ToggleTag(val tagId: Long) : SearchEvent()
    object ToggleTagMode : SearchEvent()
    data class ToggleFileType(val fileType: FileType) : SearchEvent()
    object ToggleSearchDirectories : SearchEvent()
    object ClearFilters : SearchEvent()
    data class AddToHistory(val query: String) : SearchEvent()
    object ClearHistory : SearchEvent()
    data class TagClicked(val tagId: Long) : SearchEvent()
    object ToggleSelectionMode : SearchEvent()
    data class ToggleFileSelection(val path: String) : SearchEvent()
    object ClearSelection : SearchEvent()
    object ShowTagSelector : SearchEvent()
    object HideTagSelector : SearchEvent()
    data class AddTagToSelectedFiles(val tagId: Long) : SearchEvent()
    object ClearOperationMessage : SearchEvent()
    data class TagSearchQueryChanged(val query: String) : SearchEvent()
    object ShowTagPicker : SearchEvent()
    object HideTagPicker : SearchEvent()
    data class TagPickerSearchQueryChanged(val query: String) : SearchEvent()
    data class TogglePickerTag(val tagId: Long) : SearchEvent()
    object ConfirmTagPicker : SearchEvent()
    data class TagSelectorSearchQueryChanged(val query: String) : SearchEvent()
    object ShowRemoveTagSelector : SearchEvent()
    object HideRemoveTagSelector : SearchEvent()
    data class RemoveTagSelectorSearchQueryChanged(val query: String) : SearchEvent()
    data class RemoveTagFromSelectedFiles(val tagId: Long) : SearchEvent()
}
