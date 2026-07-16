package com.tagfile.app.enhance.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.enhance.domain.model.EnhanceParams
import com.tagfile.app.enhance.domain.usecase.EnhanceImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class EnhanceViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enhanceImageUseCase: EnhanceImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnhanceUiState())
    val uiState: StateFlow<EnhanceUiState> = _uiState.asStateFlow()

    private var sourceBitmap: Bitmap? = null
    private var processedBitmap: Bitmap? = null

    fun initialize(imagePath: String) {
        _uiState.update { it.copy(sourcePath = imagePath) }
        loadSourceBitmap(imagePath)
    }

    private fun loadSourceBitmap(path: String) {
        try {
            sourceBitmap?.recycle()
            sourceBitmap = BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "无法加载图片: ${e.message}") }
        }
    }

    fun onEvent(event: EnhanceEvent) {
        when (event) {
            is EnhanceEvent.UpdateStrength -> updateParams { it.copy(strength = event.value) }
            is EnhanceEvent.UpdateSharpness -> updateParams { it.copy(sharpness = event.value) }
            is EnhanceEvent.UpdateDenoise -> updateParams { it.copy(denoise = event.value) }
            is EnhanceEvent.UpdateLineDarkening -> updateParams { it.copy(lineDarkening = event.value) }
            is EnhanceEvent.UpdateContrast -> updateParams { it.copy(contrast = event.value) }
            is EnhanceEvent.UpdateSaturation -> updateParams { it.copy(saturation = event.value) }
            is EnhanceEvent.UpdateUpscaleFactor -> updateParams { it.copy(upscaleFactor = event.value) }
            is EnhanceEvent.SelectPreset -> selectPreset(event.type)
            is EnhanceEvent.ProcessImage -> processImage()
            is EnhanceEvent.ToggleBeforeAfter -> toggleBeforeAfter()
            is EnhanceEvent.ResetToDefault -> resetToDefault()
            is EnhanceEvent.ClearError -> _uiState.update { it.copy(error = null) }
            is EnhanceEvent.SaveResult -> saveResult()
        }
    }

    private fun updateParams(transform: (EnhanceParams) -> EnhanceParams) {
        val newParams = transform(_uiState.value.params)
        _uiState.update { it.copy(params = newParams, selectedPreset = PresetType.CUSTOM) }
    }

    private fun selectPreset(type: PresetType) {
        val params = when (type) {
            PresetType.MANGA -> EnhanceParams.MANGA_PRESET
            PresetType.ANIME -> EnhanceParams.ANIME_PRESET
            PresetType.LIGHT -> EnhanceParams.LIGHT_PRESET
            PresetType.CUSTOM -> _uiState.value.params
        }
        _uiState.update { it.copy(params = params, selectedPreset = type) }
    }

    private fun processImage() {
        val source = sourceBitmap ?: run {
            _uiState.update { it.copy(error = "未加载源图片") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                processedBitmap?.recycle()
                processedBitmap = enhanceImageUseCase(source, _uiState.value.params)

                val resultBitmap = processedBitmap ?: run {
                    _uiState.update { it.copy(isProcessing = false, error = "图像处理失败") }
                    return@launch
                }

                val outputDir = File(context.cacheDir, "enhance")
                outputDir.mkdirs()
                val outputFile = File(outputDir, "enhanced_${System.currentTimeMillis()}.png")
                FileOutputStream(outputFile).use { out ->
                    resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        processedBitmapPath = outputFile.absolutePath,
                        showBefore = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = "处理失败: ${e.message}") }
            }
        }
    }

    private fun toggleBeforeAfter() {
        _uiState.update { it.copy(showBefore = !it.showBefore) }
    }

    private fun resetToDefault() {
        _uiState.update {
            it.copy(
                params = EnhanceParams.DEFAULT,
                selectedPreset = PresetType.CUSTOM,
                processedBitmapPath = null,
                showBefore = true
            )
        }
    }

    private fun saveResult() {
        val processedPath = _uiState.value.processedBitmapPath ?: return
        val sourcePath = _uiState.value.sourcePath

        viewModelScope.launch {
            try {
                val sourceFile = File(sourcePath)
                val parentDir = sourceFile.parentFile ?: return@launch
                val baseName = sourceFile.nameWithoutExtension
                val extension = sourceFile.extension.ifEmpty { "png" }

                var saveFile = File(parentDir, "${baseName}_enhanced.$extension")
                var counter = 1
                while (saveFile.exists()) {
                    saveFile = File(parentDir, "${baseName}_enhanced_$counter.$extension")
                    counter++
                }

                File(processedPath).copyTo(saveFile)
                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "保存失败: ${e.message}") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sourceBitmap?.recycle()
        processedBitmap?.recycle()
    }
}
