package com.tagfile.app.ui.settings

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class PersonalizationUiState(
    val wallpaperPath: String? = null,
    val wallpaperOpacity: Float = 0.15f,
    val textColor: Int = Color.WHITE,
    val iconColor: Int = Color.parseColor("#FF6200EE"),
    val strokeEnabled: Boolean = false,
    val strokeColor: Int = Color.BLACK
)

sealed class PersonalizationEvent {
    data class UpdateWallpaperOpacity(val value: Float) : PersonalizationEvent()
    data class SelectWallpaper(val path: String) : PersonalizationEvent()
    object RemoveWallpaper : PersonalizationEvent()
    data class UpdateTextColor(val color: Int) : PersonalizationEvent()
    data class UpdateIconColor(val color: Int) : PersonalizationEvent()
    object ToggleStroke : PersonalizationEvent()
    data class UpdateStrokeColor(val color: Int) : PersonalizationEvent()
}

@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PersonalizationUiState(
            wallpaperPath = preferencesManager.wallpaperPath.value,
            wallpaperOpacity = preferencesManager.wallpaperOpacity.value,
            textColor = preferencesManager.customTextColor.value,
            iconColor = preferencesManager.customIconColor.value,
            strokeEnabled = preferencesManager.strokeEnabled.value,
            strokeColor = preferencesManager.strokeColor.value
        )
    )
    val uiState: StateFlow<PersonalizationUiState> = _uiState.asStateFlow()

    fun onEvent(event: PersonalizationEvent) {
        when (event) {
            is PersonalizationEvent.UpdateWallpaperOpacity -> {
                _uiState.update { it.copy(wallpaperOpacity = event.value) }
                preferencesManager.setWallpaperOpacity(event.value)
            }
            is PersonalizationEvent.SelectWallpaper -> {
                viewModelScope.launch {
                    val savedPath = copyWallpaperToInternal(event.path)
                    _uiState.update { it.copy(wallpaperPath = savedPath) }
                    preferencesManager.setWallpaperPath(savedPath)
                }
            }
            is PersonalizationEvent.RemoveWallpaper -> {
                _uiState.update { it.copy(wallpaperPath = null) }
                preferencesManager.setWallpaperPath(null)
            }
            is PersonalizationEvent.UpdateTextColor -> {
                _uiState.update { it.copy(textColor = event.color) }
                preferencesManager.setCustomTextColor(event.color)
            }
            is PersonalizationEvent.UpdateIconColor -> {
                _uiState.update { it.copy(iconColor = event.color) }
                preferencesManager.setCustomIconColor(event.color)
            }
            is PersonalizationEvent.ToggleStroke -> {
                val newValue = !_uiState.value.strokeEnabled
                _uiState.update { it.copy(strokeEnabled = newValue) }
                preferencesManager.setStrokeEnabled(newValue)
            }
            is PersonalizationEvent.UpdateStrokeColor -> {
                _uiState.update { it.copy(strokeColor = event.color) }
                preferencesManager.setStrokeColor(event.color)
            }
        }
    }

    fun importWallpaper(uri: Uri) {
        viewModelScope.launch {
            try {
                val savedPath = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                    val wallDir = File(context.filesDir, "wallpaper")
                    wallDir.mkdirs()
                    val destFile = File(wallDir, "wallpaper_${System.currentTimeMillis()}")
                    FileOutputStream(destFile).use { out ->
                        inputStream.copyTo(out)
                    }
                    inputStream.close()
                    destFile.absolutePath
                }
                if (savedPath != null) {
                    _uiState.update { it.copy(wallpaperPath = savedPath) }
                    preferencesManager.setWallpaperPath(savedPath)
                }
            } catch (_: Exception) { }
        }
    }

    private suspend fun copyWallpaperToInternal(originalPath: String): String {
        return withContext(Dispatchers.IO) {
            val wallDir = File(context.filesDir, "wallpaper")
            wallDir.mkdirs()
            val destFile = File(wallDir, "wallpaper_${System.currentTimeMillis()}")
            File(originalPath).copyTo(destFile, overwrite = true)
            destFile.absolutePath
        }
    }
}
