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
        val sortModeStr = savedStateHandle.get<String>("sortMode") ?: "TITLE"
        val initialSortMode = try { BookSortMode.valueOf(sortModeStr) } catch (_: Exception) { BookSortMode.TITLE }
        _uiState.update { it.copy(query = query, searchMode = mode, sortMode = initialSortMode) }

        if (query.isBlank()) {
            loadAllBooks()
        } else {
            performSearch(query, mode)
        }
    }

    fun changeSortMode(sortMode: BookSortMode) {
        _uiState.update {
            val sorted = applySort(rawBooks, sortMode, it.sortAscending)
            it.copy(sortMode = sortMode, books = sorted, authorGroups = buildAuthorGroups(sorted, it.sortAscending), expandedAuthors = emptySet())
        }
    }

    fun toggleSortOrder() {
        _uiState.update {
            val ascending = !it.sortAscending
            val sorted = applySort(rawBooks, it.sortMode, ascending)
            it.copy(sortAscending = ascending, books = sorted, authorGroups = buildAuthorGroups(sorted, ascending))
        }
    }

    fun toggleAuthorExpanded(author: String) {
        _uiState.update {
            val expanded = it.expandedAuthors.toMutableSet()
            if (expanded.contains(author)) expanded.remove(author) else expanded.add(author)
            it.copy(expandedAuthors = expanded)
        }
    }

    private fun loadAllBooks() {
        booksJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        booksJob = viewModelScope.launch {
            shelfRepository.getAllBooks().collect { books ->
                rawBooks = books
                val state = _uiState.value
                val sorted = applySort(books, state.sortMode, state.sortAscending)
                _uiState.update { it.copy(books = sorted, authorGroups = buildAuthorGroups(sorted, state.sortAscending), isLoading = false) }
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
                val state = _uiState.value
                val sorted = applySort(results, state.sortMode, state.sortAscending)
                _uiState.update { it.copy(books = sorted, authorGroups = buildAuthorGroups(sorted, state.sortAscending), isLoading = false) }
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

    private fun buildAuthorGroups(books: List<Book>, ascending: Boolean): List<AuthorGroup> {
        val grouped = books.groupBy { it.author?.ifBlank { null } }
        val known = grouped.filterKeys { it != null }
            .map { (author, authorBooks) -> AuthorGroup(author!!, authorBooks) }
            .sortedBy { it.author.lowercase() }
        val unknown = grouped[null]?.let { listOf(AuthorGroup("未知作者", it)) } ?: emptyList()
        val ordered = if (ascending) known + unknown else known.reversed() + unknown
        return ordered
    }
}
