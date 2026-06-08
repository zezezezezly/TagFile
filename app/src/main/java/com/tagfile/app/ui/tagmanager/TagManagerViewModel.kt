package com.tagfile.app.ui.tagmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.repository.TagRepository
import com.tagfile.app.ui.theme.TagColors
import com.tagfile.app.ui.theme.toIntArgb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagManagerViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagerUiState())
    val uiState: StateFlow<TagManagerUiState> = _uiState.asStateFlow()

    init {
        loadTags()
        loadFileCounts()
    }

    fun onEvent(event: TagManagerEvent) {
        when (event) {
            is TagManagerEvent.LoadTags -> loadTags()
            is TagManagerEvent.ShowCreateDialog -> {
                _uiState.update {
                    it.copy(
                        showEditorDialog = true,
                        editingTag = null,
                        editorName = "",
                        editorColorIndex = 0
                    )
                }
            }
            is TagManagerEvent.ShowEditDialog -> {
                val tagColor = androidx.compose.ui.graphics.Color(event.tag.color)
                val colorIndex = TagColors.indexOfFirst { it.toIntArgb() == tagColor.toIntArgb() }
                    .let { if (it >= 0) it else 0 }
                _uiState.update {
                    it.copy(
                        showEditorDialog = true,
                        editingTag = event.tag,
                        editorName = event.tag.name,
                        editorColorIndex = colorIndex
                    )
                }
            }
            is TagManagerEvent.DismissEditorDialog -> {
                _uiState.update { it.copy(showEditorDialog = false) }
            }
            is TagManagerEvent.EditorNameChanged -> {
                _uiState.update { it.copy(editorName = event.name) }
            }
            is TagManagerEvent.EditorColorChanged -> {
                _uiState.update { it.copy(editorColorIndex = event.colorIndex) }
            }
            is TagManagerEvent.SaveTag -> saveTag()
            is TagManagerEvent.ShowDeleteConfirm -> {
                _uiState.update { it.copy(showDeleteConfirm = true, deleteTargetTag = event.tag) }
            }
            is TagManagerEvent.DismissDeleteConfirm -> {
                _uiState.update { it.copy(showDeleteConfirm = false, deleteTargetTag = null) }
            }
            is TagManagerEvent.ConfirmDeleteTag -> deleteTag()
            is TagManagerEvent.TagClicked -> { }
            is TagManagerEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is TagManagerEvent.ClearMessage -> {
                _uiState.update { it.copy(message = null) }
            }
            is TagManagerEvent.ToggleSortMode -> {
                _uiState.update { state ->
                    val next = when (state.sortMode) {
                        TagSortMode.DEFAULT -> TagSortMode.COLOR
                        TagSortMode.COLOR -> TagSortMode.FILE_COUNT
                        TagSortMode.FILE_COUNT -> TagSortMode.DEFAULT
                    }
                    state.copy(sortMode = next)
                }
            }
        }
    }

    private fun loadTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(tags = tags) }
            }
        }
    }

    private fun loadFileCounts() {
        viewModelScope.launch {
            try {
                val counts = tagRepository.getTagFileCounts()
                _uiState.update { it.copy(tagFileCounts = counts) }
            } catch (_: Exception) { }
        }
    }

    private fun saveTag() {
        val state = _uiState.value
        val name = state.editorName.trim()
        if (name.isEmpty()) return

        val existingNames = state.tags.map { it.name }
        if (state.editingTag == null && name in existingNames) {
            _uiState.update { it.copy(message = "标签「$name」已存在") }
            return
        }

        viewModelScope.launch {
            val color = TagColors.getOrElse(state.editorColorIndex) { TagColors[0] }
            val colorArgb = color.toIntArgb()

            state.editingTag?.let { existing ->
                tagRepository.updateTag(existing.copy(name = name, color = colorArgb))
            } ?: run {
                tagRepository.createTag(name, colorArgb)
            }

            _uiState.update { it.copy(showEditorDialog = false) }
            loadFileCounts()
        }
    }

    private fun deleteTag() {
        val tag = _uiState.value.deleteTargetTag ?: return
        viewModelScope.launch {
            tagRepository.deleteTag(tag.id)
            _uiState.update { it.copy(showDeleteConfirm = false, deleteTargetTag = null) }
            loadFileCounts()
        }
    }
}