package com.tagfile.app.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 骨架屏 Shimmer 动画色板
 */
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val shimmerHighlight = MaterialTheme.colorScheme.surfaceContainerHigh

    return Brush.linearGradient(
        colors = listOf(shimmerColor, shimmerHighlight, shimmerColor),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

/**
 * 单个骨架条
 */
@Composable
fun SkeletonBar(
    modifier: Modifier = Modifier,
    heightDp: Int = 16,
    cornerRadius: Int = 8
) {
    Box(
        modifier = modifier
            .height(heightDp.dp)
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(shimmerBrush())
    )
}

/**
 * 带图标的骨架行（模拟 ListItem）
 */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    iconSize: Int = 40
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(CircleShape)
                .background(shimmerBrush())
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonBar(modifier = Modifier.fillMaxWidth(0.6f), heightDp = 14)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBar(modifier = Modifier.fillMaxWidth(0.8f), heightDp = 12)
        }
    }
}

/**
 * 骨架屏书籍卡片
 */
@Composable
fun SkeletonBookCard(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.7f
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    Modifier.height(160.dp)
                )
                .clip(RoundedCornerShape(8.dp))
                .background(shimmerBrush())
        )
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonBar(modifier = Modifier.fillMaxWidth(), heightDp = 12)
        Spacer(modifier = Modifier.height(4.dp))
        SkeletonBar(modifier = Modifier.fillMaxWidth(0.5f), heightDp = 10)
    }
}