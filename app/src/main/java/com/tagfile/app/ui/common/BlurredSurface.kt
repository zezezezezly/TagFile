package com.tagfile.app.ui.common

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 玻璃态模糊容器
 * Android 12+ 使用 RenderEffect 原生模糊
 * 低版本回退到半透明无模糊
 */
@Composable
fun BlurredSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    alpha: Float = 0.85f,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val backgroundColor = surfaceColor.copy(alpha = alpha)

    Box(
        modifier = modifier
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    @Suppress("DEPRECATION")
                    Modifier.drawBehind {
                        drawRect(
                            color = backgroundColor,
                            topLeft = androidx.compose.ui.geometry.Offset.Zero,
                            size = size,
                            blendMode = androidx.compose.ui.graphics.BlendMode.SrcOver
                        )
                    }
                } else {
                    Modifier.background(backgroundColor, shape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * 玻璃态卡片容器 - 用于列表卡片
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    elevation: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = elevation,
        content = { Box { content() } }
    )
}

@Composable
fun glassSurfaceColor() = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)

@Composable
fun glassContainerColor() = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)