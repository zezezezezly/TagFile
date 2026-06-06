@file:OptIn(ExperimentalFoundationApi::class)

package com.tagfile.app.ui.imageviewer

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoFixNormal
import androidx.compose.material.icons.filled.AutoFixOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerScreen(
    imagePaths: List<String>,
    initialIndex: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToEnhance: (String) -> Unit = {},
    viewModel: ImageViewerViewModel = hiltViewModel()
) {
    val validPaths = imagePaths.filter { File(it).exists() }
    if (validPaths.isEmpty()) {
        onNavigateBack()
        return
    }

    val startIndex = initialIndex.coerceIn(0, (validPaths.size - 1).coerceAtLeast(0))
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { validPaths.size })
    var isImmersive by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageLoader = context.imageLoader
    val scope = rememberCoroutineScope()

    val view = LocalView.current

    DisposableEffect(Unit) {
        val window = (view.context as Activity).window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = false
        controller.isAppearanceLightNavigationBars = false
        onDispose {
            val ctrl = WindowCompat.getInsetsController(window, window.decorView)
            ctrl.isAppearanceLightStatusBars = true
            ctrl.isAppearanceLightNavigationBars = true
            viewModel.clearEnhanced()
        }
    }

    val currentPath = validPaths[pagerState.currentPage]
    LaunchedEffect(currentPath, uiState.isContinuousEnhance) {
        while (pagerState.isScrollInProgress) {
            kotlinx.coroutines.delay(50)
        }
        viewModel.enhanceImage(currentPath)
    }

    LaunchedEffect(pagerState.currentPage) {
        val current = pagerState.currentPage
        if (pagerState.isScrollInProgress) {
            kotlinx.coroutines.delay(150)
            if (pagerState.isScrollInProgress) return@LaunchedEffect
        }
        val start = (current - 3).coerceAtLeast(0)
        val end = (current + 3).coerceAtMost(validPaths.size - 1)
        for (i in start..end) {
            if (i != current) {
                imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(File(validPaths[i]))
                        .build()
                )
            }
        }
    }

    val showEnhanced = !pagerState.isScrollInProgress &&
        uiState.showEnhanced && uiState.enhancedBitmap != null &&
        uiState.enhancingPath == currentPath

    val thumbnailListState = rememberLazyListState()
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val spacerWidth = screenWidthDp / 2 - 17.dp

    LaunchedEffect(pagerState.currentPage) {
        val itemPx = with(density) { 34.dp.toPx() }
        val viewportPx = with(density) { screenWidthDp.toPx() }
        val offset = -(viewportPx / 2 - itemPx / 2).toInt()
        thumbnailListState.animateScrollToItem(pagerState.currentPage + 1, offset)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isImmersive = !isImmersive
            }
    ) {
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val path = validPaths[page]
            val isCurrentEnhanced = showEnhanced && path == currentPath

            var scale by remember(page) { mutableFloatStateOf(1f) }
            var offsetX by remember(page) { mutableFloatStateOf(0f) }
            var offsetY by remember(page) { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        }
                        .pointerInput(page) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                var shouldEndGesture = false

                                while (!shouldEndGesture) {
                                    val event = awaitPointerEvent()
                                    val changes = event.changes
                                    val anyPressed = changes.any { it.pressed }

                                    when {
                                        changes.size >= 2 -> {
                                            val c1 = changes[0]
                                            val c2 = changes[1]
                                            val currentCentroid = Offset(
                                                (c1.position.x + c2.position.x) / 2f,
                                                (c1.position.y + c2.position.y) / 2f
                                            )
                                            val prevCentroid = Offset(
                                                (c1.previousPosition.x + c2.previousPosition.x) / 2f,
                                                (c1.previousPosition.y + c2.previousPosition.y) / 2f
                                            )
                                            val currentDist = (c1.position - c2.position).getDistance()
                                            val prevDist = (c1.previousPosition - c2.previousPosition).getDistance()
                                            val zoomFactor = if (prevDist > 0f) currentDist / prevDist else 1f
                                            val panDelta = (currentCentroid - prevCentroid) * scale

                                            val newScale = (scale * zoomFactor).coerceIn(1f, 5f)
                                            scale = newScale
                                            if (newScale > 1f) {
                                                offsetX = (offsetX + panDelta.x).coerceIn(
                                                    -(size.width * (newScale - 1f)) / 2f,
                                                    (size.width * (newScale - 1f)) / 2f
                                                )
                                                offsetY = (offsetY + panDelta.y).coerceIn(
                                                    -(size.height * (newScale - 1f)) / 2f,
                                                    (size.height * (newScale - 1f)) / 2f
                                                )
                                            } else {
                                                scale = 1f; offsetX = 0f; offsetY = 0f
                                            }
                                            changes.forEach { it.consume() }
                                        }

                                        changes.size == 1 && scale > 1f -> {
                                            val change = changes[0]
                                            if (change.pressed) {
                                                val drag = (change.position - change.previousPosition) * scale
                                                val maxOffsetX = (size.width * (scale - 1f)) / 2f
                                                val maxOffsetY = (size.height * (scale - 1f)) / 2f

                                                val atLeftEdge = offsetX >= maxOffsetX - 0.5f
                                                val atRightEdge = offsetX <= -maxOffsetX + 0.5f

                                                if ((atLeftEdge && drag.x > 0f) || (atRightEdge && drag.x < 0f)) {
                                                    val targetPage = if (drag.x > 0f) pagerState.currentPage - 1 else pagerState.currentPage + 1
                                                    if (targetPage in 0 until validPaths.size) {
                                                        scope.launch { pagerState.animateScrollToPage(targetPage) }
                                                    }
                                                    scale = 1f; offsetX = 0f; offsetY = 0f
                                                    shouldEndGesture = true
                                                    changes.forEach { it.consume() }
                                                } else {
                                                    offsetX = (offsetX + drag.x).coerceIn(-maxOffsetX, maxOffsetX)
                                                    offsetY = (offsetY + drag.y).coerceIn(-maxOffsetY, maxOffsetY)
                                                    change.consume()
                                                }
                                            }
                                        }

                                        else -> {
                                            // scale == 1f: don't consume, let pager handle
                                        }
                                    }

                                    if (!anyPressed) break
                                }
                            }
                        }
                ) {
                    AsyncImage(
                        model = File(path),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    if (isCurrentEnhanced) {
                        androidx.compose.foundation.Image(
                            bitmap = uiState.enhancedBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        if (!isImmersive) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                    Row {
                        IconButton(onClick = { viewModel.toggleContinuousEnhance() }) {
                            Icon(
                                imageVector = if (uiState.isContinuousEnhance)
                                    Icons.Default.AutoFixHigh
                                else
                                    Icons.Default.AutoFixOff,
                                contentDescription = "持续增强",
                                tint = if (uiState.isContinuousEnhance)
                                    Color(0xFF4CAF50)
                                else
                                    Color.White.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(onClick = {
                            if (validPaths.isNotEmpty()) {
                                onNavigateToEnhance(validPaths[pagerState.currentPage])
                            }
                        }) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = "画质增强",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${validPaths.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(vertical = 8.dp)
                ) {
                    LazyRow(
                        state = thumbnailListState,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item { Spacer(modifier = Modifier.width(spacerWidth)) }
                        itemsIndexed(validPaths, key = { index, _ -> index }) { index, path ->
                            val isCurrent = index == pagerState.currentPage
                            AsyncImage(
                                model = File(path),
                                imageLoader = imageLoader,
                                contentDescription = null,
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(34.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .then(
                                        if (isCurrent) {
                                            Modifier.border(2.dp, Color.White, RoundedCornerShape(3.dp))
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable {
                                        scope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                contentScale = ContentScale.Crop
                            )
                        }
                        item { Spacer(modifier = Modifier.width(spacerWidth)) }
                    }
                }
            }
        }
    }
}
