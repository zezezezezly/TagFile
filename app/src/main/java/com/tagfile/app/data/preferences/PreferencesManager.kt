package com.tagfile.app.data.preferences

import android.content.Context
import android.graphics.Color
import com.tagfile.app.enhance.domain.model.EnhanceParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("tagfile_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    private val _continuousEnhance = MutableStateFlow(prefs.getBoolean("continuous_enhance", false))
    val continuousEnhance: StateFlow<Boolean> = _continuousEnhance.asStateFlow()

    fun setContinuousEnhance(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_enhance", enabled).apply()
        _continuousEnhance.value = enabled
    }

    private val _wallpaperPath = MutableStateFlow(prefs.getString("wallpaper_path", null))
    val wallpaperPath: StateFlow<String?> = _wallpaperPath.asStateFlow()

    private val _wallpaperOpacity = MutableStateFlow(prefs.getFloat("wallpaper_opacity", 0.15f))
    val wallpaperOpacity: StateFlow<Float> = _wallpaperOpacity.asStateFlow()

    fun setWallpaperPath(path: String?) {
        prefs.edit().putString("wallpaper_path", path).apply()
        _wallpaperPath.value = path
    }

    fun setWallpaperOpacity(opacity: Float) {
        prefs.edit().putFloat("wallpaper_opacity", opacity.coerceIn(0.02f, 1f)).apply()
        _wallpaperOpacity.value = opacity
    }

    private val _shelfFolderPath = MutableStateFlow(prefs.getString("shelf_folder_path", null))
    val shelfFolderPath: StateFlow<String?> = _shelfFolderPath.asStateFlow()

    fun setShelfFolderPath(path: String?) {
        prefs.edit().putString("shelf_folder_path", path).apply()
        _shelfFolderPath.value = path
    }

    private val _customTextColor = MutableStateFlow(prefs.getInt("custom_text_color", Color.WHITE))
    val customTextColor: StateFlow<Int> = _customTextColor.asStateFlow()

    private val _customIconColor = MutableStateFlow(prefs.getInt("custom_icon_color", Color.parseColor("#FF6200EE")))
    val customIconColor: StateFlow<Int> = _customIconColor.asStateFlow()

    fun setCustomTextColor(color: Int) {
        prefs.edit().putInt("custom_text_color", color).apply()
        _customTextColor.value = color
    }

    fun setCustomIconColor(color: Int) {
        prefs.edit().putInt("custom_icon_color", color).apply()
        _customIconColor.value = color
    }

    private val _strokeEnabled = MutableStateFlow(prefs.getBoolean("stroke_enabled", false))
    val strokeEnabled: StateFlow<Boolean> = _strokeEnabled.asStateFlow()

    fun setStrokeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("stroke_enabled", enabled).apply()
        _strokeEnabled.value = enabled
    }

    private val _strokeColor = MutableStateFlow(prefs.getInt("stroke_color", Color.BLACK))
    val strokeColor: StateFlow<Int> = _strokeColor.asStateFlow()

    fun setStrokeColor(color: Int) {
        prefs.edit().putInt("stroke_color", color).apply()
        _strokeColor.value = color
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

    fun getRecommendationDate(): String = prefs.getString("recommendation_date", "") ?: ""

    fun setRecommendationDate(date: String) {
        prefs.edit().putString("recommendation_date", date).apply()
    }
}
