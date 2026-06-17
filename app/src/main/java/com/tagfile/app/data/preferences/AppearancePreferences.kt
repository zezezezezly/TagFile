package com.tagfile.app.data.preferences

import android.content.Context
import android.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class AppShortcut(
    val type: String, // "folder", "tag", "book"
    val path: String,
    val label: String
)

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

    private val _hasWallpaper = MutableStateFlow(
        prefs.getString("wallpaper_path", null)?.let { File(it).exists() } == true
    )
    val hasWallpaper: StateFlow<Boolean> = _hasWallpaper.asStateFlow()

    private val _wallpaperOpacity = MutableStateFlow(prefs.getFloat("wallpaper_opacity", 0.15f))
    val wallpaperOpacity: StateFlow<Float> = _wallpaperOpacity.asStateFlow()

    fun refreshWallpaper() {
        val path = prefs.getString("wallpaper_path", null)
        _hasWallpaper.value = path != null && File(path).exists()
        _wallpaperOpacity.value = prefs.getFloat("wallpaper_opacity", 0.15f)
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

    // region Shortcuts

    private val _shortcuts = MutableStateFlow(loadShortcuts())
    val shortcuts: StateFlow<List<AppShortcut>> = _shortcuts.asStateFlow()

    fun addShortcut(shortcut: AppShortcut) {
        val updated = _shortcuts.value.toMutableList().apply { add(shortcut) }
        saveShortcuts(updated)
        _shortcuts.value = updated
    }

    fun removeShortcut(index: Int) {
        val updated = _shortcuts.value.toMutableList().apply { removeAt(index) }
        saveShortcuts(updated)
        _shortcuts.value = updated
    }

    private fun loadShortcuts(): List<AppShortcut> {
        val json = prefs.getString("shortcuts", null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                AppShortcut(
                    type = obj.getString("type"),
                    path = obj.getString("path"),
                    label = obj.getString("label")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveShortcuts(shortcuts: List<AppShortcut>) {
        val arr = JSONArray()
        shortcuts.forEach { s ->
            arr.put(JSONObject().apply {
                put("type", s.type)
                put("path", s.path)
                put("label", s.label)
            })
        }
        prefs.edit().putString("shortcuts", arr.toString()).apply()
    }

    // endregion
}
