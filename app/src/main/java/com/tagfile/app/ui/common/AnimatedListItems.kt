package com.tagfile.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 带交错入场动画的 LazyColumn 包装.
 * 每项延迟 40ms 依次淡入+上浮入场.
 */
@Composable
fun <T> AnimatedLazyColumn(
    items: List<T>,
    modifier: Modifier = Modifier,
    key: (T) -> Any = { it.hashCode() },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    delayPerItemMs: Int = 40,
    initialOffsetDp: Int = 20,
    itemContent: @Composable (T, Int, Boolean) -> Unit
) {
    val visibilityState = remember(items.size) { mutableStateListOf<Boolean>() }

    LaunchedEffect(items.size) {
        val currentSize = visibilityState.size
        if (currentSize < items.size) {
            repeat(items.size - currentSize) { visibilityState.add(false) }
        }
        items.forEachIndexed { index, _ ->
            kotlinx.coroutines.delay(delayPerItemMs.toLong())
            visibilityState[index] = true
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        itemsIndexed(items, key = { i, item -> key(item) }) { index, item ->
            val visible = visibilityState.getOrElse(index) { false }
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = 0.7f),
                    initialOffsetY = { fullHeight -> (fullHeight * initialOffsetDp / 20).coerceAtLeast(initialOffsetDp) }
                ) + fadeIn(animationSpec = tween(300)),
            ) {
                itemContent(item, index, visible)
            }
        }
    }
}

/**
 * 扩展函数：在 LazyListScope 中添加带交错入场动画的 indexed items.
 * 用于已有 LazyColumn 的 DSL 内部.
 */
inline fun <T> LazyListScope.animatedItemsIndexed(
    items: List<T>,
    crossinline key: (Int, T) -> Any = { _, item -> item.hashCode() },
    noinline delayPerItemMs: () -> Int = { 40 },
    noinline visibleState: () -> List<Boolean>,
    crossinline itemContent: @Composable (T, Int, Boolean) -> Unit
) {
    itemsIndexed(items, key = { i, item -> key(i, item) }) { index, item ->
        val visible = visibleState().getOrElse(index) { false }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                animationSpec = spring(dampingRatio = 0.7f),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = tween(300)),
        ) {
            itemContent(item, index, visible)
        }
    }
}