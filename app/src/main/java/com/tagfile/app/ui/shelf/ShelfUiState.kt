package com.tagfile.app.ui.shelf

import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.repository.SearchMode

data class ShelfUiState(
    val books: List<Book> = emptyList(),
    val recommendations: List<Book> = emptyList(),
    val searchQuery: String = "",
    val searchMode: SearchMode = SearchMode.TITLE,
    val isScanning: Boolean = false,
    val isLoading: Boolean = false,
    val shelfPath: String? = null,
    val operationMessage: String? = null
)

sealed class ShelfEvent {
    data class SearchQueryChanged(val query: String) : ShelfEvent()
    data class SearchModeChanged(val mode: SearchMode) : ShelfEvent()
    object ScanBooks : ShelfEvent()
    object ClearOperationMessage : ShelfEvent()
    object RefreshRecommendations : ShelfEvent()
}
