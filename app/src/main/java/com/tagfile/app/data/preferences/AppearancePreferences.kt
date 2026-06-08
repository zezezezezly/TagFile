package com.tagfile.app.data.preferences

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppearancePreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("tagfile_prefs", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
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
}
