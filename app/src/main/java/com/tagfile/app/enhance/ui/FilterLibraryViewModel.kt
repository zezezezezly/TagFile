package com.tagfile.app.enhance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.preferences.PreferencesManager
import com.tagfile.app.enhance.data.repository.FilterPreset
import com.tagfile.app.enhance.data.repository.FilterPresetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterLibraryUiState(
    val filters: List<FilterPreset> = emptyList(),
    val activeFilterId: Long = -1L
)

@HiltViewModel
class FilterLibraryViewModel @Inject constructor(
    private val repository: FilterPresetRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        FilterLibraryUiState(activeFilterId = preferencesManager.getActiveFilterPresetId())
    )
    val uiState: StateFlow<FilterLibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { filters ->
                _uiState.value = _uiState.value.copy(filters = filters)
            }
        }
    }

    fun selectFilter(filter: FilterPreset) {
        preferencesManager.setActiveFilterPresetId(filter.id)
        _uiState.value = _uiState.value.copy(activeFilterId = filter.id)
    }

    fun deleteFilter(filter: FilterPreset) {
        viewModelScope.launch {
            if (_uiState.value.activeFilterId == filter.id) {
                preferencesManager.setActiveFilterPresetId(-1L)
                _uiState.value = _uiState.value.copy(activeFilterId = -1L)
            }
            repository.delete(filter.id)
        }
    }
}
