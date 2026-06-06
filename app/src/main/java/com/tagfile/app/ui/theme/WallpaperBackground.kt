package com.tagfile.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import coil.compose.AsyncImage
import com.tagfile.app.data.preferences.PreferencesManager
import java.io.File

@Composable
fun WallpaperBackground(
    preferencesManager: PreferencesManager,
    content: @Composable () -> Unit
) {
    val wallpaperPath by preferencesManager.wallpaperPath.collectAsState()
    val wallpaperOpacity by preferencesManager.wallpaperOpacity.collectAsState()
    val hasWallpaper = wallpaperPath != null && File(wallpaperPath!!).exists()
    val view = LocalView.current

    if (hasWallpaper) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }

        val transparentScheme = MaterialTheme.colorScheme.copy(
            background = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            surface = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
            surfaceVariant = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
            surfaceContainerHighest = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0f),
            surfaceContainerHigh = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0f),
            surfaceContainer = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0f),
            surfaceContainerLow = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0f),
            surfaceContainerLowest = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0f),
            surfaceDim = MaterialTheme.colorScheme.surfaceDim.copy(alpha = 0f),
            surfaceBright = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0f),
            surfaceTint = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0f)
        )

        MaterialTheme(colorScheme = transparentScheme) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = File(wallpaperPath!!),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(wallpaperOpacity),
                    contentScale = ContentScale.Crop
                )

                content()
            }
        }
    } else {
        content()
    }
}
