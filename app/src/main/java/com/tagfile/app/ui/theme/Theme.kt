package com.tagfile.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.tagfile.app.data.preferences.AppearancePreferences

// 分级圆角：大16dp / 中12dp / 小8dp
val AppShapes = Shapes(
    extraLarge = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(8.dp),
    extraSmall = RoundedCornerShape(8.dp)
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.12f),
    onPrimaryContainer = PrimaryVariant,
    secondary = PrimaryVariant,
    onSecondary = OnPrimary,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OnSurfaceVariantLight.copy(alpha = 0.2f),
    outlineVariant = OnSurfaceVariantLight.copy(alpha = 0.1f),
    error = Color(0xFFC62828),
    onError = OnPrimary,
    surfaceContainerLowest = SurfaceLight,
    surfaceContainerLow = SurfaceVariantLight,
    surfaceContainer = SurfaceVariantLight,
    surfaceContainerHigh = Color(0xFFE0DCD6),
    surfaceContainerHighest = Color(0xFFD8D4CE),
    surfaceDim = Color(0xFFD0CCC6),
    surfaceBright = SurfaceLight,
    surfaceTint = Primary.copy(alpha = 0.05f)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    onPrimary = PrimaryVariant,
    primaryContainer = Color(0xFF4DB6AC).copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFFB2DFDB),
    secondary = Color(0xFF80CBC4),
    onSecondary = PrimaryVariant,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OnSurfaceVariantDark.copy(alpha = 0.2f),
    outlineVariant = OnSurfaceVariantDark.copy(alpha = 0.1f),
    error = Color(0xFFEF5350),
    onError = OnPrimary,
    surfaceContainerLowest = Color(0xFF15171A),
    surfaceContainerLow = SurfaceDark,
    surfaceContainer = SurfaceDark,
    surfaceContainerHigh = Color(0xFF282A2C),
    surfaceContainerHighest = Color(0xFF2C2F30),
    surfaceDim = Color(0xFF101214),
    surfaceBright = Color(0xFF303336),
    surfaceTint = Color(0xFF4DB6AC).copy(alpha = 0.05f)
)

@Composable
fun TagFileTheme(
    appearancePreferences: AppearancePreferences,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val strokeEnabled by appearancePreferences.strokeEnabled.collectAsState()
    val strokeColorInt by appearancePreferences.strokeColor.collectAsState()

    val hasWallpaper by appearancePreferences.hasWallpaper.collectAsState()
    val wallpaperOpacity by appearancePreferences.wallpaperOpacity.collectAsState()

    val strokeColor = Color(strokeColorInt)

    val colorScheme = if (hasWallpaper) {
        // 有壁纸时：背景半透明，文字根据壁纸自动适应
        val isDarkWallpaper = darkTheme
        val textColor = if (isDarkWallpaper) OnBackgroundDark else OnBackgroundLight
        baseScheme.copy(
            background = baseScheme.background.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surface = baseScheme.surface.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceVariant = baseScheme.surfaceVariant.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceContainerLowest = baseScheme.surfaceContainerLowest.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceContainerLow = baseScheme.surfaceContainerLow.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceContainer = baseScheme.surfaceContainer.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceContainerHigh = baseScheme.surfaceContainerHigh.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceContainerHighest = baseScheme.surfaceContainerHighest.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceDim = baseScheme.surfaceDim.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            surfaceBright = baseScheme.surfaceBright.copy(alpha = 1f - wallpaperOpacity * 0.85f),
            onBackground = textColor,
            onSurface = textColor,
            onSurfaceVariant = textColor.copy(alpha = 0.7f)
        )
    } else {
        baseScheme
    }

    val typography = if (strokeEnabled) {
        val s = Shadow(color = strokeColor, offset = Offset.Zero, blurRadius = 2f)
        Typography(
            displayLarge = AppTypography.displayLarge.copy(shadow = s),
            displayMedium = AppTypography.displayMedium.copy(shadow = s),
            displaySmall = AppTypography.displaySmall.copy(shadow = s),
            headlineLarge = AppTypography.headlineLarge.copy(shadow = s),
            headlineMedium = AppTypography.headlineMedium.copy(shadow = s),
            headlineSmall = AppTypography.headlineSmall.copy(shadow = s),
            titleLarge = AppTypography.titleLarge.copy(shadow = s),
            titleMedium = AppTypography.titleMedium.copy(shadow = s),
            titleSmall = AppTypography.titleSmall.copy(shadow = s),
            bodyLarge = AppTypography.bodyLarge.copy(shadow = s),
            bodyMedium = AppTypography.bodyMedium.copy(shadow = s),
            bodySmall = AppTypography.bodySmall.copy(shadow = s),
            labelLarge = AppTypography.labelLarge.copy(shadow = s),
            labelMedium = AppTypography.labelMedium.copy(shadow = s),
            labelSmall = AppTypography.labelSmall.copy(shadow = s)
        )
    } else {
        AppTypography
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            val controller = WindowCompat.getInsetsController(window, view)
            if (darkTheme) {
                controller.isAppearanceLightStatusBars = false
            } else {
                controller.isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = AppShapes,
        content = content
    )
}
