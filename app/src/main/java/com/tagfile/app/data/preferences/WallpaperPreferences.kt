package com.tagfile.app.data.preferences

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WallpaperPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("tagfile_prefs", Context.MODE_PRIVATE)

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
}
