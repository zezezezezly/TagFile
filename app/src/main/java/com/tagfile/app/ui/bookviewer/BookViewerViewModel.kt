package com.tagfile.app.ui.bookviewer

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewerViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookViewerUiState())
    val uiState: StateFlow<BookViewerUiState> = _uiState.asStateFlow()

    private val startTime = SystemClock.elapsedRealtime()
    private var isObserving = false

    init {
        val bookId = savedStateHandle.get<Long>("bookId") ?: 0L
        if (bookId > 0) {
            loadBook(bookId)
        }
    }

    fun onEvent(event: BookViewerEvent) {
        when (event) {
            is BookViewerEvent.PageChanged -> {
                _uiState.update { it.copy(currentIndex = event.index) }
            }
        }
    }

    private fun loadBook(bookId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val book = shelfRepository.getBookById(bookId)
                if (book != null) {
                    val images = shelfRepository.getImagesInBook(book)
                    _uiState.update {
                        it.copy(
                            book = book,
                            images = images,
                            currentIndex = book.currentPage,
                            isLoading = false
                        )
                    }
                    shelfRepository.incrementViewCount(bookId)
                    // Start observing page changes only once
                    if (!isObserving) {
                        isObserving = true
                        observePageChanges(bookId)
                    }
                } else {
                    _uiState.update { it.copy(error = "书籍不存在", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun observePageChanges(bookId: Long) {
        viewModelScope.launch {
            _uiState
                .map { it.currentIndex }
                .distinctUntilChanged()
                .collectLatest { currentPage ->
                    delay(500)
                    saveProgress(bookId, currentPage)
                }
        }
    }

    private suspend fun saveProgress(bookId: Long, currentPage: Int) {
        shelfRepository.updateCurrentPage(bookId, currentPage)
    }

    override fun onCleared() {
        super.onCleared()
        val bookId = savedStateHandle.get<Long>("bookId") ?: return
        val elapsed = SystemClock.elapsedRealtime() - startTime
        viewModelScope.launch {
            shelfRepository.addDuration(bookId, elapsed)
        }
    }
}
