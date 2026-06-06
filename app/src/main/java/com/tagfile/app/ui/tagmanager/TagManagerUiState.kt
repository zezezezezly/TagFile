package com.tagfile.app.ui.tagmanager

import com.tagfile.app.domain.model.Tag

enum class TagSortMode { DEFAULT, COLOR, FILE_COUNT }

data class TagManagerUiState(
    val tags: List<Tag> = emptyList(),
    val tagFileCounts: Map<Long, Int> = emptyMap(),
    val sortMode: TagSortMode = TagSortMode.DEFAULT,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showEditorDialog: Boolean = false,
    val editingTag: Tag? = null,
    val editorName: String = "",
    val editorColorIndex: Int = 0,
    val showDeleteConfirm: Boolean = false,
    val deleteTargetTag: Tag? = null,
    val message: String? = null
) {
    val filteredTags: List<Tag>
        get() {
            val base = if (searchQuery.isBlank()) tags
            else tags.filter { it.name.contains(searchQuery, ignoreCase = true) }
            return when (sortMode) {
                TagSortMode.COLOR -> base.sortedBy { it.color }
                TagSortMode.FILE_COUNT -> base.sortedByDescending { tagFileCounts[it.id] ?: 0 }
                TagSortMode.DEFAULT -> base
            }
        }
}

sealed class TagManagerEvent {
    object LoadTags : TagManagerEvent()
    object ShowCreateDialog : TagManagerEvent()
    data class ShowEditDialog(val tag: Tag) : TagManagerEvent()
    object DismissEditorDialog : TagManagerEvent()
    data class EditorNameChanged(val name: String) : TagManagerEvent()
    data class EditorColorChanged(val colorIndex: Int) : TagManagerEvent()
    object SaveTag : TagManagerEvent()
    data class ShowDeleteConfirm(val tag: Tag) : TagManagerEvent()
    object DismissDeleteConfirm : TagManagerEvent()
    object ConfirmDeleteTag : TagManagerEvent()
    data class TagClicked(val tagId: Long) : TagManagerEvent()
    data class SearchQueryChanged(val query: String) : TagManagerEvent()
    object ClearMessage : TagManagerEvent()
    object ToggleSortMode : TagManagerEvent()
}