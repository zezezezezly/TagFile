package com.tagfile.app.ui.shelf

import com.tagfile.app.domain.model.Book

enum class BookSortMode { TITLE, AUTHOR, PAGE_COUNT, VIEW_COUNT, SCORE }

data class AuthorGroup(
    val author: String,
    val books: List<Book>
)

data class BookListUiState(
    val books: List<Book> = emptyList(),
    val sortMode: BookSortMode = BookSortMode.TITLE,
    val sortAscending: Boolean = true,
    val isLoading: Boolean = false,
    val query: String = "",
    val searchMode: String = "TITLE",
    val authorGroups: List<AuthorGroup> = emptyList(),
    val expandedAuthors: Set<String> = emptySet()
)
