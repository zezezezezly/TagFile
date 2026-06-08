package com.tagfile.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
import androidx.core.view.WindowCompat
import com.tagfile.app.data.preferences.AppearancePreferences

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
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

    val customTextColor by appearancePreferences.customTextColor.collectAsState()
    val customIconColor by appearancePreferences.customIconColor.collectAsState()
    val strokeEnabled by appearancePreferences.strokeEnabled.collectAsState()
    val strokeColorInt by appearancePreferences.strokeColor.collectAsState()

    val textColor = Color(customTextColor)
    val iconColor = Color(customIconColor)
    val strokeColor = Color(strokeColorInt)

    val colorScheme = baseScheme.copy(
        onBackground = textColor,
        onSurface = textColor,
        onSurfaceVariant = textColor.copy(alpha = 0.7f),
        primary = iconColor
    )

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
            if (darkTheme) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            } else {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
