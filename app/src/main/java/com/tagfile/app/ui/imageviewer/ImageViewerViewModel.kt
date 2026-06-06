package com.tagfile.app.ui.imageviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.preferences.PreferencesManager
import com.tagfile.app.enhance.data.repository.FilterPresetRepository
import com.tagfile.app.enhance.domain.usecase.EnhanceImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ImageViewerUiState(
    val enhancedBitmap: Bitmap? = null,
    val enhancingPath: String? = null,
    val showEnhanced: Boolean = false,
    val isContinuousEnhance: Boolean = true
)

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val enhanceImageUseCase: EnhanceImageUseCase,
    private val filterRepository: FilterPresetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ImageViewerUiState(isContinuousEnhance = preferencesManager.continuousEnhance.value)
    )
    val uiState: StateFlow<ImageViewerUiState> = _uiState.asStateFlow()

    private var currentJob: kotlinx.coroutines.Job? = null
    private var pendingRecycleBitmap: Bitmap? = null

    fun toggleContinuousEnhance() {
        val newValue = !_uiState.value.isContinuousEnhance
        preferencesManager.setContinuousEnhance(newValue)
        _uiState.update { it.copy(isContinuousEnhance = newValue) }
        if (!newValue) {
            clearEnhanced()
        }
    }

    fun enhanceImage(imagePath: String) {
        if (!_uiState.value.isContinuousEnhance) return
        if (_uiState.value.enhancingPath == imagePath && _uiState.value.showEnhanced) return

        currentJob?.cancel()

        pendingRecycleBitmap?.recycle()
        pendingRecycleBitmap = _uiState.value.enhancedBitmap
        _uiState.update { it.copy(enhancingPath = imagePath, enhancedBitmap = null, showEnhanced = false) }

        currentJob = viewModelScope.launch(Dispatchers.Default) {
            val source = try {
                BitmapFactory.decodeFile(imagePath)
            } catch (e: Exception) {
                null
            }
            if (source == null) {
                _uiState.update { it.copy(enhancedBitmap = null, enhancingPath = null, showEnhanced = false) }
                return@launch
            }

            try {
                val params = withContext(Dispatchers.Default) {
                    val activeId = preferencesManager.getActiveFilterPresetId()
                    if (activeId > 0) {
                        filterRepository.getParamsById(activeId)
                            ?: preferencesManager.getEnhanceParams()
                    } else {
                        preferencesManager.getEnhanceParams()
                    }
                }

                val enhanced = withContext(Dispatchers.Default) {
                    enhanceImageUseCase(source, params)
                }
                source.recycle()

                _uiState.update {
                    if (it.enhancingPath == imagePath) {
                        it.copy(enhancedBitmap = enhanced, showEnhanced = true)
                    } else {
                        enhanced.recycle()
                        it
                    }
                }
            } catch (e: Exception) {
                source.recycle()
                _uiState.update {
                    if (it.enhancingPath == imagePath) {
                        it.copy(enhancedBitmap = null, enhancingPath = null, showEnhanced = false)
                    } else it
                }
            }
        }
    }

    fun clearEnhanced() {
        _uiState.value.enhancedBitmap?.recycle()
        pendingRecycleBitmap?.recycle()
        pendingRecycleBitmap = null
        _uiState.update {
            it.copy(enhancedBitmap = null, enhancingPath = null, showEnhanced = false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
        _uiState.value.enhancedBitmap?.recycle()
        pendingRecycleBitmap?.recycle()
    }
}
