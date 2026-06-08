package com.tagfile.app.ui.shelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.preferences.ShelfPreferences
import com.tagfile.app.domain.repository.SearchMode
import com.tagfile.app.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShelfViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val shelfPreferences: ShelfPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShelfUiState())
    val uiState: StateFlow<ShelfUiState> = _uiState.asStateFlow()

    private var booksJob: Job? = null

    init {
        _uiState.update { it.copy(shelfPath = shelfPreferences.shelfFolderPath.value) }

        viewModelScope.launch {
            shelfPreferences.shelfFolderPath.collect { path ->
                _uiState.update { it.copy(shelfPath = path) }
                if (_uiState.value.searchQuery.isBlank()) {
                    loadAllBooks()
                } else {
                    performSearch(_uiState.value.searchQuery, _uiState.value.searchMode)
                }
            }
        }

        loadAllBooks()
        loadRecommendations()
        loadRecentlyRead()
    }

    fun onEvent(event: ShelfEvent) {
        when (event) {
            is ShelfEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                if (event.query.isBlank()) {
                    loadAllBooks()
                }
            }
            is ShelfEvent.SearchModeChanged -> {
                _uiState.update { it.copy(searchMode = event.mode) }
                if (_uiState.value.searchQuery.isNotBlank()) {
                    performSearch(_uiState.value.searchQuery, event.mode)
                }
            }
            is ShelfEvent.ScanBooks -> scanBooks()
            is ShelfEvent.ClearOperationMessage -> _uiState.update { it.copy(operationMessage = null) }
            is ShelfEvent.RefreshRecommendations -> refreshRecommendations()
        }
    }

    fun applySearch() {
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            performSearch(query, _uiState.value.searchMode)
        }
    }

    private fun loadAllBooks() {
        booksJob?.cancel()
        val path = _uiState.value.shelfPath
        if (path.isNullOrBlank()) {
            _uiState.update { it.copy(books = emptyList(), isLoading = false) }
            return
        }
        _uiState.update { it.copy(isLoading = true) }
        booksJob = viewModelScope.launch {
            shelfRepository.getAllBooks().collect { books ->
                _uiState.update { it.copy(books = books, isLoading = false) }
            }
        }
    }

    private fun loadRecommendations() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = shelfPreferences.getRecommendationDate()
        if (today == savedDate && _uiState.value.recommendations.isNotEmpty()) return
        val seed = today.hashCode().toLong()
        doRefreshRecommendations(seed)
    }

    private fun refreshRecommendations() {
        val seed = System.currentTimeMillis()
        doRefreshRecommendations(seed)
    }

    private fun doRefreshRecommendations(seed: Long) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        shelfPreferences.setRecommendationDate(today)
        viewModelScope.launch {
            try {
                val books = shelfRepository.getDailyRecommendations(3, seed)
                _uiState.update { it.copy(recommendations = books) }
            } catch (_: Exception) { }
        }
    }

    private fun loadRecentlyRead() {
        viewModelScope.launch {
            shelfRepository.getRecentlyReadBooks(10).collect { books ->
                _uiState.update { it.copy(recentlyRead = books) }
            }
        }
    }

    private fun performSearch(query: String, mode: SearchMode) {
        booksJob?.cancel()
        _uiState.update { it.copy(isLoading = true) }
        booksJob = viewModelScope.launch {
            shelfRepository.searchBooks(query, mode).collect { results ->
                _uiState.update { it.copy(books = results, isLoading = false) }
            }
        }
    }

    private fun scanBooks() {
        val path = _uiState.value.shelfPath ?: return
        _uiState.update { it.copy(isScanning = true) }
        viewModelScope.launch {
            try {
                val newBooks = shelfRepository.scanAndAddBooks(path)
                _uiState.update {
                    it.copy(isScanning = false, operationMessage = "已添加 ${newBooks.size} 本新书")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isScanning = false, operationMessage = "扫描失败: ${e.message}")
                }
            }
        }
    }
}
