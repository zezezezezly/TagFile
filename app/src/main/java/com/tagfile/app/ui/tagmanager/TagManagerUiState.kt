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
    val editorGroupName: String = "",
    val showDeleteConfirm: Boolean = false,
    val deleteTargetTag: Tag? = null,
    val message: String? = null,
    val allGroups: List<String> = emptyList(),
    val showGroupManagement: Boolean = false,
    val groupManagementMessage: String? = null,
    val collapsedGroups: Set<String> = emptySet()
) {
    val filteredTags: List<Tag>
        get() {
            val base = tags.let {
                if (searchQuery.isBlank()) it
                else it.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            return when (sortMode) {
                TagSortMode.COLOR -> base.sortedBy { it.color }
                TagSortMode.FILE_COUNT -> base.sortedByDescending { tagFileCounts[it.id] ?: 0 }
                TagSortMode.DEFAULT -> base
            }
        }

    data class GroupedTags(
        val groupName: String?,
        val tags: List<Tag>
    )

    val groupedTags: List<GroupedTags>
        get() {
            if (searchQuery.isNotBlank()) {
                return emptyList()
            }
            val grouped = tags.filter { !it.groupName.isNullOrBlank() }
                .groupBy { it.groupName!! }
                .toSortedMap() // Alphabetical by group name
                .toMutableMap()
            val ungrouped = tags.filter { it.groupName.isNullOrBlank() }
            val result = grouped.entries.map { GroupedTags(it.key, sortGroupTags(it.value)) }.toMutableList()
            if (ungrouped.isNotEmpty()) {
                result.add(GroupedTags(null, sortGroupTags(ungrouped)))
            }
            return result
        }

    private fun sortGroupTags(tags: List<Tag>): List<Tag> {
        return when (sortMode) {
            TagSortMode.COLOR -> tags.sortedBy { it.color }
            TagSortMode.FILE_COUNT -> tags.sortedByDescending { tagFileCounts[it.id] ?: 0 }
            TagSortMode.DEFAULT -> tags
        }
    }

    fun isCollapsed(groupName: String?): Boolean {
        return collapsedGroups.contains(groupName ?: "ungrouped")
    }

    val ungroupedCount: Int
        get() = tags.count { it.groupName.isNullOrBlank() }
}

sealed class TagManagerEvent {
    object LoadTags : TagManagerEvent()
    object ShowCreateDialog : TagManagerEvent()
    data class ShowCreateDialogWithGroup(val groupName: String?) : TagManagerEvent()
    data class ShowEditDialog(val tag: Tag) : TagManagerEvent()
    object DismissEditorDialog : TagManagerEvent()
    data class EditorNameChanged(val name: String) : TagManagerEvent()
    data class EditorColorChanged(val colorIndex: Int) : TagManagerEvent()
    data class EditorGroupNameChanged(val groupName: String) : TagManagerEvent()
    object SaveTag : TagManagerEvent()
    data class ShowDeleteConfirm(val tag: Tag) : TagManagerEvent()
    object DismissDeleteConfirm : TagManagerEvent()
    object ConfirmDeleteTag : TagManagerEvent()
    data class TagClicked(val tagId: Long) : TagManagerEvent()
    data class SearchQueryChanged(val query: String) : TagManagerEvent()
    object ClearMessage : TagManagerEvent()
    object ToggleSortMode : TagManagerEvent()
    object ShowGroupManagement : TagManagerEvent()
    object DismissGroupManagement : TagManagerEvent()
    data class RenameGroup(val oldName: String, val newName: String) : TagManagerEvent()
    data class DeleteGroup(val groupName: String) : TagManagerEvent()
    data class MergeGroups(val fromGroup: String, val toGroup: String) : TagManagerEvent()
    object DismissGroupManagementMessage : TagManagerEvent()
    data class ToggleGroupCollapse(val groupKey: String) : TagManagerEvent()
    data class MoveUngroupedToGroup(val targetGroup: String) : TagManagerEvent()
}