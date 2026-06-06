package com.tagfile.app.ui.shelf

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.repository.SearchMode
import com.tagfile.app.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()
    private var booksJob: Job? = null
    private var rawBooks: List<Book> = emptyList()

    init {
        val query = savedStateHandle.get<String>("query") ?: ""
        val mode = savedStateHandle.get<String>("mode") ?: "TITLE"
        _uiState.update { it.copy(query = query, searchMode = mode) }

        if (query.isBlank()) {
            loadAllBooks()
        } else {
            performSearch(query, mode)
        }
    }

    fun changeSortMode(sortMode: BookSortMode) {
        _uiState.update { it.copy(sortMode = sortMode, books = applySort(rawBooks, sortMode, it.sortAscending)) }
    }

    fun toggleSortOrder() {
        _uiState.update { it.copy(sortAscending = !it.sortAscending, books = applySort(rawBooks, it.sortMode, !it.sortAscending)) }
    }

    private fun loadAllBooks() {
        booksJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        booksJob = viewModelScope.launch {
            shelfRepository.getAllBooks().collect { books ->
                rawBooks = books
                _uiState.update { it.copy(books = applySort(books, _uiState.value.sortMode, _uiState.value.sortAscending), isLoading = false) }
            }
        }
    }

    private fun performSearch(query: String, mode: String) {
        booksJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        val searchMode = try { SearchMode.valueOf(mode) } catch (_: Exception) { SearchMode.TITLE }
        booksJob = viewModelScope.launch {
            shelfRepository.searchBooks(query, searchMode).collect { results ->
                rawBooks = results
                _uiState.update { it.copy(books = applySort(results, _uiState.value.sortMode, _uiState.value.sortAscending), isLoading = false) }
            }
        }
    }

    private fun applySort(books: List<Book>, sortMode: BookSortMode, ascending: Boolean): List<Book> {
        val sorted = when (sortMode) {
            BookSortMode.TITLE -> books.sortedBy { it.title.lowercase() }
            BookSortMode.AUTHOR -> books.sortedBy { (it.author ?: "").lowercase() }
            BookSortMode.PAGE_COUNT -> books.sortedByDescending { it.pageCount }
            BookSortMode.VIEW_COUNT -> books.sortedByDescending { it.viewCount }
            BookSortMode.SCORE -> books.sortedByDescending { it.score }
        }
        return if (ascending) sorted else sorted.reversed()
    }
}
