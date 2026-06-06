package com.tagfile.app.ui.bookviewer

import com.tagfile.app.domain.model.Book

data class BookViewerUiState(
    val book: Book? = null,
    val images: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class BookViewerEvent {
    data class PageChanged(val index: Int) : BookViewerEvent()
}
