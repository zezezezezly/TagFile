package com.tagfile.app.ui.bookviewer

import android.graphics.Bitmap
import com.tagfile.app.domain.model.Book

data class BookViewerUiState(
    val book: Book? = null,
    val images: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val enhancedBitmap: Bitmap? = null,
    val enhancingPath: String? = null,
    val showEnhanced: Boolean = false,
    val isContinuousEnhance: Boolean = true,
    val saveMessage: String? = null
)
