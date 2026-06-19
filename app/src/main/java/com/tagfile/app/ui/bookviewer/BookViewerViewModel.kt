package com.tagfile.app.ui.bookviewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.preferences.EnhancePreferences
import com.tagfile.app.domain.repository.ShelfRepository
import com.tagfile.app.enhance.data.repository.FilterPresetRepository
import com.tagfile.app.enhance.domain.usecase.EnhanceImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class BookViewerViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val savedStateHandle: SavedStateHandle,
    private val enhancePreferences: EnhancePreferences,
    private val enhanceImageUseCase: EnhanceImageUseCase,
    private val filterRepository: FilterPresetRepository
) : ViewModel(), DefaultLifecycleObserver {

    private var sessionStartTime = 0L
    private var accumulatedDuration = 0L
    private var isResumed = false

    fun bindLifecycle(lifecycle: LifecycleOwner) {
        lifecycle.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        isResumed = true
        sessionStartTime = SystemClock.elapsedRealtime()
    }

    override fun onPause(owner: LifecycleOwner) {
        if (isResumed) {
            val sessionDuration = SystemClock.elapsedRealtime() - sessionStartTime
            accumulatedDuration += sessionDuration
            isResumed = false
            if (sessionDuration > 0) {
                viewModelScope.launch {
                    shelfRepository.addReadDuration(bookId, sessionDuration, System.currentTimeMillis())
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isResumed) {
            val pending = SystemClock.elapsedRealtime() - sessionStartTime
            accumulatedDuration += pending
            isResumed = false
            if (pending > 0) {
                viewModelScope.launch {
                    shelfRepository.addReadDuration(bookId, pending, System.currentTimeMillis())
                }
            }
        }
        currentJob?.cancel()
        _uiState.value.enhancedBitmap?.recycle()
        pendingRecycleBitmap?.recycle()
    }

    private val bookId: Long = savedStateHandle.get<Long>("bookId") ?: 0L
    private val _uiState = MutableStateFlow(
        BookViewerUiState(isContinuousEnhance = enhancePreferences.continuousEnhance.value)
    )
    val uiState: StateFlow<BookViewerUiState> = _uiState.asStateFlow()

    private var currentJob: kotlinx.coroutines.Job? = null
    private var pendingRecycleBitmap: Bitmap? = null

    init {
        loadBook()
        startAutoSave()
    }

    private fun loadBook() {
        viewModelScope.launch {
            val book = shelfRepository.getBookById(bookId)
            if (book != null) {
                _uiState.update {
                    it.copy(
                        book = book,
                        images = shelfRepository.getImagesInBook(book),
                        currentIndex = book.currentPage,
                        isLoading = false
                    )
                }
                shelfRepository.incrementViewCount(bookId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "书籍不存在") }
            }
        }
    }

    private fun startAutoSave() {
        viewModelScope.launch {
            _uiState.map { it.currentIndex }
                .distinctUntilChanged()
                .collectLatest { page ->
                    delay(500)
                    shelfRepository.updateCurrentPage(bookId, page)
                }
        }
    }

    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentIndex = page) }
    }

    fun toggleContinuousEnhance() {
        val newValue = !_uiState.value.isContinuousEnhance
        enhancePreferences.setContinuousEnhance(newValue)
        _uiState.update { it.copy(isContinuousEnhance = newValue) }
        if (!newValue) {
            clearEnhanced()
        }
    }

    fun enhanceImage(imagePath: String) {
        if (!_uiState.value.isContinuousEnhance) return
        if (_uiState.value.enhancingPath == imagePath && _uiState.value.showEnhanced) return

        currentJob?.cancel()

        pendingRecycleBitmap?.recycle()
        pendingRecycleBitmap = _uiState.value.enhancedBitmap
        _uiState.update { it.copy(enhancingPath = imagePath, enhancedBitmap = null, showEnhanced = false) }

        currentJob = viewModelScope.launch(Dispatchers.Default) {
            val source = try {
                BitmapFactory.decodeFile(imagePath)
            } catch (e: Exception) {
                null
            }
            if (source == null) {
                _uiState.update { it.copy(enhancedBitmap = null, enhancingPath = null, showEnhanced = false) }
                return@launch
            }

            try {
                val params = withContext(Dispatchers.Default) {
                    val activeId = enhancePreferences.getActiveFilterPresetId()
                    if (activeId > 0) {
                        filterRepository.getParamsById(activeId)
                            ?: enhancePreferences.getEnhanceParams()
                    } else {
                        enhancePreferences.getEnhanceParams()
                    }
                }

                val enhanced = withContext(Dispatchers.Default) {
                    enhanceImageUseCase(source, params)
                }
                source.recycle()

                _uiState.update {
                    if (it.enhancingPath == imagePath) {
                        it.copy(enhancedBitmap = enhanced, showEnhanced = true)
                    } else {
                        enhanced.recycle()
                        it
                    }
                }
            } catch (e: Exception) {
                source.recycle()
                _uiState.update {
                    if (it.enhancingPath == imagePath) {
                        it.copy(enhancedBitmap = null, enhancingPath = null, showEnhanced = false)
                    } else it
                }
            }
        }
    }

    fun clearEnhanced() {
        _uiState.value.enhancedBitmap?.recycle()
        pendingRecycleBitmap?.recycle()
        pendingRecycleBitmap = null
        _uiState.update {
            it.copy(enhancedBitmap = null, enhancingPath = null, showEnhanced = false)
        }
    }

    fun saveEnhancedImage() {
        val bitmap = _uiState.value.enhancedBitmap ?: return
        val sourcePath = _uiState.value.enhancingPath ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sourceFile = File(sourcePath)
                val parentDir = sourceFile.parentFile ?: return@launch
                val nameWithoutExt = sourceFile.nameWithoutExtension
                val extension = sourceFile.extension.ifEmpty { "jpg" }
                val saveFile = File(parentDir, "${nameWithoutExt}_enhanced.$extension")
                FileOutputStream(saveFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                _uiState.update { it.copy(saveMessage = "已保存: ${saveFile.name}") }
            } catch (e: Exception) {
                _uiState.update { it.copy(saveMessage = "保存失败: ${e.message}") }
            }
        }
    }

    fun clearSaveMessage() {
        _uiState.update { it.copy(saveMessage = null) }
    }
}
