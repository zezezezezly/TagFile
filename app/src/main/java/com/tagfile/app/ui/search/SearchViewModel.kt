package com.tagfile.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.model.SearchFilter
import com.tagfile.app.domain.model.TagMode
import com.tagfile.app.domain.repository.TagRepository
import com.tagfile.app.domain.usecase.SearchFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchFilesUseCase: SearchFilesUseCase,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadAllTags()
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.QueryChanged -> {
                _uiState.update { it.copy(query = event.query) }
                debounceSearch()
            }
            is SearchEvent.ClearQuery -> {
                _uiState.update { it.copy(query = "", results = emptyList()) }
            }
            is SearchEvent.PerformSearch -> performSearch()
            is SearchEvent.ToggleTag -> {
                _uiState.update { state ->
                    val newTags = state.selectedTagIds.toMutableSet()
                    if (event.tagId in newTags) newTags.remove(event.tagId)
                    else newTags.add(event.tagId)
                    state.copy(selectedTagIds = newTags)
                }
                performSearch()
            }
            is SearchEvent.ToggleTagMode -> {
                _uiState.update { state ->
                    state.copy(
                        tagMode = if (state.tagMode == TagMode.AND) TagMode.OR else TagMode.AND
                    )
                }
                performSearch()
            }
            is SearchEvent.ToggleFileType -> {
                _uiState.update { state ->
                    val newTypes = state.selectedFileTypes.toMutableSet()
                    if (event.fileType in newTypes) newTypes.remove(event.fileType)
                    else newTypes.add(event.fileType)
                    state.copy(selectedFileTypes = newTypes)
                }
                performSearch()
            }
            is SearchEvent.ToggleSearchDirectories -> {
                _uiState.update { it.copy(searchDirectories = !it.searchDirectories) }
                performSearch()
            }
            is SearchEvent.ClearFilters -> {
                _uiState.update {
                    it.copy(
                        selectedTagIds = emptySet(),
                        selectedFileTypes = emptySet(),
                        searchDirectories = false,
                        tagMode = TagMode.AND,
                        tagSearchQuery = ""
                    )
                }
                performSearch()
            }
            is SearchEvent.AddToHistory -> addToHistory(event.query)
            is SearchEvent.ClearHistory -> _uiState.update { it.copy(searchHistory = emptyList()) }
            is SearchEvent.TagClicked -> { }
            is SearchEvent.ToggleSelectionMode -> {
                _uiState.update {
                    if (it.isSelectionMode) it.copy(isSelectionMode = false, selectedPaths = emptySet())
                    else it.copy(isSelectionMode = true)
                }
            }
            is SearchEvent.ToggleFileSelection -> {
                _uiState.update { state ->
                    val newPaths = state.selectedPaths.toMutableSet()
                    if (event.path in newPaths) newPaths.remove(event.path)
                    else newPaths.add(event.path)
                    if (newPaths.isEmpty()) state.copy(selectedPaths = newPaths, isSelectionMode = false)
                    else state.copy(selectedPaths = newPaths)
                }
            }
            is SearchEvent.ClearSelection -> {
                _uiState.update { it.copy(isSelectionMode = false, selectedPaths = emptySet()) }
            }
            is SearchEvent.ShowTagSelector -> {
                _uiState.update { it.copy(showTagSelector = true, tagSelectorSearchQuery = "") }
            }
            is SearchEvent.HideTagSelector -> {
                _uiState.update { it.copy(showTagSelector = false) }
            }
            is SearchEvent.AddTagToSelectedFiles -> {
                addTagToSelectedFiles(event.tagId)
            }
            is SearchEvent.ClearOperationMessage -> {
                _uiState.update { it.copy(operationMessage = null) }
            }
            is SearchEvent.TagSearchQueryChanged -> {
                _uiState.update { it.copy(tagSearchQuery = event.query) }
            }
            is SearchEvent.ShowTagPicker -> {
                _uiState.update { it.copy(showTagPicker = true, tagPickerSearchQuery = "", pickerSelectedTagIds = it.selectedTagIds) }
            }
            is SearchEvent.HideTagPicker -> {
                _uiState.update { it.copy(showTagPicker = false) }
            }
            is SearchEvent.TagPickerSearchQueryChanged -> {
                _uiState.update { it.copy(tagPickerSearchQuery = event.query) }
            }
            is SearchEvent.TogglePickerTag -> {
                _uiState.update { state ->
                    val newTags = state.pickerSelectedTagIds.toMutableSet()
                    if (event.tagId in newTags) newTags.remove(event.tagId)
                    else newTags.add(event.tagId)
                    state.copy(pickerSelectedTagIds = newTags)
                }
            }
            is SearchEvent.ConfirmTagPicker -> {
                _uiState.update { it.copy(showTagPicker = false, selectedTagIds = it.pickerSelectedTagIds) }
                performSearch()
            }
            is SearchEvent.TagSelectorSearchQueryChanged -> {
                _uiState.update { it.copy(tagSelectorSearchQuery = event.query) }
            }
            is SearchEvent.ShowRemoveTagSelector -> {
                _uiState.update { it.copy(showRemoveTagSelector = true, removeTagSelectorSearchQuery = "") }
            }
            is SearchEvent.HideRemoveTagSelector -> {
                _uiState.update { it.copy(showRemoveTagSelector = false) }
            }
            is SearchEvent.RemoveTagSelectorSearchQueryChanged -> {
                _uiState.update { it.copy(removeTagSelectorSearchQuery = event.query) }
            }
            is SearchEvent.RemoveTagFromSelectedFiles -> {
                removeTagFromSelectedFiles(event.tagId)
            }
        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            tagRepository.getAllTags().collect { tags ->
                _uiState.update { it.copy(allTags = tags) }
            }
        }
    }

    private fun debounceSearch() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch()
        }
    }

    private fun performSearch() {
        val state = _uiState.value
        if (state.query.isBlank() && state.selectedTagIds.isEmpty()
            && state.selectedFileTypes.isEmpty() && !state.searchDirectories) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        _uiState.update { it.copy(isSearching = true) }

        viewModelScope.launch {
            val filter = SearchFilter(
                keyword = state.query,
                tagIds = state.selectedTagIds.toList(),
                tagMode = state.tagMode,
                fileTypes = state.selectedFileTypes,
                searchDirectories = state.searchDirectories
            )

            searchFilesUseCase(filter).fold(
                onSuccess = { files ->
                    _uiState.update { it.copy(results = files, isSearching = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(results = emptyList(), isSearching = false) }
                }
            )
        }
    }

    private fun addToHistory(query: String) {
        if (query.isBlank()) return
        _uiState.update { state ->
            val history = state.searchHistory.toMutableList()
            history.remove(query)
            history.add(0, query)
            state.copy(searchHistory = history.take(10))
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
        }
    }
}
