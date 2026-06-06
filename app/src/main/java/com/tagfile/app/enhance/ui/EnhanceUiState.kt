package com.tagfile.app.enhance.ui

import com.tagfile.app.enhance.domain.model.EnhanceParams

data class EnhanceUiState(
    val sourcePath: String = "",
    val params: EnhanceParams = EnhanceParams(),
    val isProcessing: Boolean = false,
    val processedBitmapPath: String? = null,
    val error: String? = null,
    val showBefore: Boolean = true,
    val selectedPreset: PresetType = PresetType.CUSTOM
)

enum class PresetType(val label: String) {
    CUSTOM("自定义"),
    MANGA("漫画增强"),
    ANIME("动画增强"),
    LIGHT("轻度增强")
}

sealed interface EnhanceEvent {
    data class UpdateStrength(val value: Float) : EnhanceEvent
    data class UpdateSharpness(val value: Float) : EnhanceEvent
    data class UpdateDenoise(val value: Float) : EnhanceEvent
    data class UpdateLineDarkening(val value: Float) : EnhanceEvent
    data class UpdateContrast(val value: Float) : EnhanceEvent
    data class UpdateSaturation(val value: Float) : EnhanceEvent
    data class UpdateUpscaleFactor(val value: Int) : EnhanceEvent
    data class SelectPreset(val type: PresetType) : EnhanceEvent
    data object ProcessImage : EnhanceEvent
    data object ToggleBeforeAfter : EnhanceEvent
    data object ResetToDefault : EnhanceEvent
    data object ClearError : EnhanceEvent
    data object SaveResult : EnhanceEvent
}
