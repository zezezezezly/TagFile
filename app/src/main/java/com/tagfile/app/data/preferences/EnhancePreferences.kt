package com.tagfile.app.data.preferences

import android.content.Context
import com.tagfile.app.enhance.domain.model.EnhanceParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnhancePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("tagfile_prefs", Context.MODE_PRIVATE)

    private val _continuousEnhance = MutableStateFlow(prefs.getBoolean("continuous_enhance", false))
    val continuousEnhance: StateFlow<Boolean> = _continuousEnhance.asStateFlow()

    fun setContinuousEnhance(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_enhance", enabled).apply()
        _continuousEnhance.value = enabled
    }

    fun getActiveFilterPresetId(): Long {
        return prefs.getLong("active_filter_id", -1L)
    }

    fun setActiveFilterPresetId(id: Long) {
        prefs.edit().putLong("active_filter_id", id).apply()
    }

    fun getEnhanceParams(): EnhanceParams {
        return EnhanceParams(
            strength = prefs.getFloat("enhance_strength", 0.5f),
            sharpness = prefs.getFloat("enhance_sharpness", 0.5f),
            denoise = prefs.getFloat("enhance_denoise", 0.3f),
            lineDarkening = prefs.getFloat("enhance_line_darkening", 0.5f),
            contrast = prefs.getFloat("enhance_contrast", 0.3f),
            saturation = prefs.getFloat("enhance_saturation", 0.2f),
            upscaleFactor = prefs.getInt("enhance_upscale", 1)
        )
    }

    fun saveEnhanceParams(params: EnhanceParams) {
        prefs.edit()
            .putFloat("enhance_strength", params.strength)
            .putFloat("enhance_sharpness", params.sharpness)
            .putFloat("enhance_denoise", params.denoise)
            .putFloat("enhance_line_darkening", params.lineDarkening)
            .putFloat("enhance_contrast", params.contrast)
            .putFloat("enhance_saturation", params.saturation)
            .putInt("enhance_upscale", params.upscaleFactor)
            .apply()
    }
}
