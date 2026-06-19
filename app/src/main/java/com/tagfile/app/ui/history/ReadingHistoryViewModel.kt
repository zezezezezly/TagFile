package com.tagfile.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReadingHistoryUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReadingHistoryViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingHistoryUiState())
    val uiState: StateFlow<ReadingHistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            shelfRepository.getReadHistory().collect { books ->
                _uiState.update { it.copy(books = books, isLoading = false) }
            }
        }
    }
}