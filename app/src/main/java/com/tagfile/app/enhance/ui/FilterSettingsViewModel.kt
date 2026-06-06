package com.tagfile.app.enhance.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.enhance.data.repository.FilterPresetRepository
import com.tagfile.app.enhance.domain.model.EnhanceParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterSettingsUiState(
    val filterId: Long = -1L,
    val name: String = "",
    val params: EnhanceParams = EnhanceParams.DEFAULT,
    val isNew: Boolean = true,
    val isSaved: Boolean = false
)

sealed class FilterSettingsEvent {
    data class UpdateName(val value: String) : FilterSettingsEvent()
    data class UpdateStrength(val value: Float) : FilterSettingsEvent()
    data class UpdateSharpness(val value: Float) : FilterSettingsEvent()
    data class UpdateDenoise(val value: Float) : FilterSettingsEvent()
    data class UpdateLineDarkening(val value: Float) : FilterSettingsEvent()
    data class UpdateContrast(val value: Float) : FilterSettingsEvent()
    data class UpdateSaturation(val value: Float) : FilterSettingsEvent()
    data class UpdateUpscaleFactor(val value: Int) : FilterSettingsEvent()
    data class SelectPreset(val type: PresetType) : FilterSettingsEvent()
    data object Save : FilterSettingsEvent()
    data object ClearSaved : FilterSettingsEvent()
}

@HiltViewModel
class FilterSettingsViewModel @Inject constructor(
    private val repository: FilterPresetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val filterId: Long = savedStateHandle.get<Long>("filterId") ?: -1L
    private val isNew = filterId <= 0L

    private val _uiState = MutableStateFlow(
        FilterSettingsUiState(filterId = filterId, isNew = isNew)
    )
    val uiState: StateFlow<FilterSettingsUiState> = _uiState.asStateFlow()

    init {
        if (!isNew) {
            viewModelScope.launch {
                val preset = repository.getById(filterId)
                if (preset != null) {
                    _uiState.value = FilterSettingsUiState(
                        filterId = filterId,
                        name = preset.name,
                        params = preset.params,
                        isNew = false
                    )
                }
            }
        }
    }

    fun onEvent(event: FilterSettingsEvent) {
        when (event) {
            is FilterSettingsEvent.UpdateName -> _uiState.update { it.copy(name = event.value, isSaved = false) }
            is FilterSettingsEvent.UpdateStrength -> updateParams { it.copy(strength = event.value) }
            is FilterSettingsEvent.UpdateSharpness -> updateParams { it.copy(sharpness = event.value) }
            is FilterSettingsEvent.UpdateDenoise -> updateParams { it.copy(denoise = event.value) }
            is FilterSettingsEvent.UpdateLineDarkening -> updateParams { it.copy(lineDarkening = event.value) }
            is FilterSettingsEvent.UpdateContrast -> updateParams { it.copy(contrast = event.value) }
            is FilterSettingsEvent.UpdateSaturation -> updateParams { it.copy(saturation = event.value) }
            is FilterSettingsEvent.UpdateUpscaleFactor -> updateParams { it.copy(upscaleFactor = event.value) }
            is FilterSettingsEvent.SelectPreset -> {
                val params = when (event.type) {
                    PresetType.MANGA -> EnhanceParams.MANGA_PRESET
                    PresetType.ANIME -> EnhanceParams.ANIME_PRESET
                    PresetType.LIGHT -> EnhanceParams.LIGHT_PRESET
                    PresetType.CUSTOM -> _uiState.value.params
                }
                _uiState.update { it.copy(params = params, isSaved = false) }
            }
            is FilterSettingsEvent.Save -> save()
            is FilterSettingsEvent.ClearSaved -> _uiState.update { it.copy(isSaved = false) }
        }
    }

    private fun updateParams(transform: (EnhanceParams) -> EnhanceParams) {
        _uiState.update { it.copy(params = transform(it.params), isSaved = false) }
    }

    private fun save() {
        val state = _uiState.value
        val name = state.name.ifBlank { "未命名滤镜" }
        viewModelScope.launch {
            if (state.isNew) {
                val newId = repository.create(name, state.params)
                _uiState.update { it.copy(filterId = newId, isNew = false, name = name, isSaved = true) }
            } else {
                repository.update(state.filterId, name, state.params)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}
