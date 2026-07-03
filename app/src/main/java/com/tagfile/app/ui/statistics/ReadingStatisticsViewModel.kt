package com.tagfile.app.ui.statistics

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

data class ReadingStatisticsUiState(
    val totalDuration: Long = 0,
    val readBookCount: Int = 0,
    val activeDays: Int = 0,
    val topBooks: List<Book> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ReadingStatisticsViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReadingStatisticsUiState())
    val uiState: StateFlow<ReadingStatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val totalDuration = shelfRepository.getTotalReadDuration()
            val readBookCount = shelfRepository.getReadBookCount()
            val activeDays = shelfRepository.getActiveDays()
            val topBooks = shelfRepository.getTopBooksByDuration(10)
            _uiState.update {
                it.copy(
                    totalDuration = totalDuration,
                    readBookCount = readBookCount,
                    activeDays = activeDays,
                    topBooks = topBooks,
                    isLoading = false
                )
            }
        }
    }
}