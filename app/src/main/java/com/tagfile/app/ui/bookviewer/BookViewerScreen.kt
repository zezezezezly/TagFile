@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.tagfile.app.ui.bookviewer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BookViewerScreen(
    viewModel: BookViewerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val imageLoader = context.imageLoader

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = uiState.book?.title ?: "书籍")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "未知错误",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                uiState.images.isNotEmpty() -> {
                    val pagerState = rememberPagerState(
                        initialPage = uiState.currentIndex,
                        pageCount = { uiState.images.size }
                    )
                    val scope = rememberCoroutineScope()
                    val thumbnailListState = rememberLazyListState()
                    val density = LocalDensity.current
                    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
                    val spacerWidth = screenWidthDp / 2 - 17.dp

                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.onEvent(BookViewerEvent.PageChanged(pagerState.currentPage))
                    }

                    LaunchedEffect(pagerState.currentPage) {
                        val itemPx = with(density) { 34.dp.toPx() }
                        val viewportPx = with(density) { screenWidthDp.toPx() }
                        val offset = -(viewportPx / 2 - itemPx / 2).toInt()
                        thumbnailListState.animateScrollToItem(pagerState.currentPage + 1, offset)
                    }

                    LaunchedEffect(pagerState.currentPage) {
                        val current = pagerState.currentPage
                        if (pagerState.isScrollInProgress) {
                            kotlinx.coroutines.delay(150)
                            if (pagerState.isScrollInProgress) return@LaunchedEffect
                        }
                        val images = uiState.images
                        val start = (current - 3).coerceAtLeast(0)
                        val end = (current + 3).coerceAtMost(images.size - 1)
                        for (i in start..end) {
                            if (i != current) {
                                imageLoader.enqueue(
                                    ImageRequest.Builder(context)
                                        .data(File(images[i]))
                                        .build()
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        HorizontalPager(
                            state = pagerState,
                            beyondBoundsPageCount = 2,
                            modifier = Modifier.weight(1f)
                        ) { page ->
                            val imagePath = uiState.images[page]
                            AsyncImage(
                                model = File(imagePath),
                                imageLoader = imageLoader,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 8.dp)
                        ) {
                            LazyRow(
                                state = thumbnailListState,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                item { Spacer(modifier = Modifier.width(spacerWidth)) }
                                itemsIndexed(uiState.images, key = { index, _ -> index }) { index, path ->
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
                                                    Modifier.border(
                                                        2.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                        RoundedCornerShape(3.dp)
                                                    )
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

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "没有图片",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
