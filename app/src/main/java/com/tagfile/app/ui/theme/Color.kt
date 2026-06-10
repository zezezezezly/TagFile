package com.tagfile.app.ui.theme

import androidx.compose.ui.graphics.Color

// 主色：湖蓝 Teal 00897B
val Primary = Color(0xFF00897B)
val PrimaryVariant = Color(0xFF00695C)
val PrimaryLight = Color(0xFF43A047)

// 背景色：暖灰 Warm Gray
val BackgroundLight = Color(0xFFF5F0EB)
val SurfaceLight = Color(0xFFF8F4F0)
val SurfaceVariantLight = Color(0xFFE8E4DE)

// 深色模式：非纯黑深灰
val BackgroundDark = Color(0xFF1A1D1F)
val SurfaceDark = Color(0xFF1E2123)
val SurfaceVariantDark = Color(0xFF25282A)

// On 文字颜色
val OnPrimary = Color.White
val OnBackgroundLight = Color(0xFF1B1C1E)
val OnBackgroundDark = Color(0xFFE8E6E3)
val OnSurfaceVariantLight = Color(0xFF65635F)
val OnSurfaceVariantDark = Color(0xFFA9A6A0)

// 标签颜色 - 降低饱和度 20%
val TagColors = listOf(
    Color(0xFFCC5252),  // #E53935 → 降饱和
    Color(0xFFC8507A),  // #D81B60
    Color(0xFF8A4090),  // #8E24AA
    Color(0xFF6E4BA0),  // #5E35B1
    Color(0xFF4554A0),  // #3949AB
    Color(0xFF357DC9),  // #1E88E5
    Color(0xFF208EC8),  // #039BE5
    Color(0xFF189991),  // #00ACC1
    Color(0xFF00786B),  // #00897B (原色，已是低饱和)
    Color(0xFF509E50),  // #43A047
    Color(0xFF80AD52),  // #7CB342
    Color(0xFFF07040),  // #F4511E
)

fun Color.toIntArgb(): Int {
    val a = (alpha * 255f + 0.5f).toInt()
    val r = (red * 255f + 0.5f).toInt()
    val g = (green * 255f + 0.5f).toInt()
    val b = (blue * 255f + 0.5f).toInt()
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

fun Int.toTagColorOrGray(): Color {
    return try { Color(this) } catch (_: Exception) { Color.Gray }
}
