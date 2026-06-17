package com.tagfile.app.ui.shelf

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.domain.repository.ShelfRepository
import com.tagfile.app.ui.theme.TagColors
import com.tagfile.app.ui.theme.toIntArgb
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = false,
    val editDescription: String = "",
    val isSaving: Boolean = false,
    val message: String? = null,
    val showTagEditor: Boolean = false,
    val allTags: List<Tag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val newTagName: String = "",
    val newTagColorIndex: Int = 0,
    val showScoreEditor: Boolean = false,
    val editScoreText: String = "",
    val showAuthorEditor: Boolean = false,
    val editAuthorName: String = "",
    val showCoverPicker: Boolean = false,
    val coverPickerImages: List<String> = emptyList()
)

sealed class BookDetailEvent {
    data class SelectCover(val imagePath: String) : BookDetailEvent()
    data object ToggleCoverPicker : BookDetailEvent()
}

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    init {
        val bookId = savedStateHandle.get<Long>("bookId") ?: 0L
        if (bookId > 0) loadBook(bookId)
        loadAllTags()
    }

    fun onEvent(event: BookDetailEvent) {
        when (event) {
            is BookDetailEvent.SelectCover -> saveCover(event.imagePath)
            is BookDetailEvent.ToggleCoverPicker -> {
                val current = _uiState.value.showCoverPicker
                if (!current) {
                    val book = _uiState.value.book
                    if (book != null) {
                        val images = shelfRepository.getImagesInBook(book)
                        _uiState.update {
                            it.copy(
                                showCoverPicker = true,
                                coverPickerImages = images
                            )
                        }
                    }
                } else {
                    _uiState.update { it.copy(showCoverPicker = false) }
                }
            }
        }
    }

    private fun loadBook(bookId: Long) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val book = shelfRepository.getBookById(bookId)
            _uiState.update {
                it.copy(book = book, isLoading = false, editDescription = book?.description ?: "")
            }
        }
    }

    private fun loadAllTags() {
        viewModelScope.launch {
            shelfRepository.getAllBookTags().collect { tags ->
                _uiState.update { it.copy(allTags = tags) }
            }
        }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(editDescription = value) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun saveDescription() {
        val book = _uiState.value.book ?: return
        val newDesc = _uiState.value.editDescription
        if (newDesc == book.description) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                shelfRepository.updateBookDescription(book.id, newDesc)
                val updated = book.copy(description = newDesc)
                _uiState.update {
                    it.copy(book = updated, isSaving = false, message = "简介已保存")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, message = "保存失败: ${e.message}")
                }
            }
        }
    }

    fun showTagEditor() {
        val book = _uiState.value.book ?: return
        val currentTagNames = book.tags.split("/").filter { it.isNotBlank() }.toSet()
        val allTags = _uiState.value.allTags
        val selectedIds = allTags.filter { it.name in currentTagNames }.map { it.id }.toSet()
        _uiState.update {
            it.copy(showTagEditor = true, selectedTagIds = selectedIds, newTagName = "", newTagColorIndex = 0)
        }
    }

    fun dismissTagEditor() {
        _uiState.update { it.copy(showTagEditor = false) }
    }

    fun toggleTag(tagId: Long) {
        _uiState.update { state ->
            val newSelected = if (tagId in state.selectedTagIds) {
                state.selectedTagIds - tagId
            } else {
                state.selectedTagIds + tagId
            }
            state.copy(selectedTagIds = newSelected)
        }
    }

    fun onNewTagNameChanged(name: String) {
        _uiState.update { it.copy(newTagName = name) }
    }

    fun onNewTagColorChanged(index: Int) {
        _uiState.update { it.copy(newTagColorIndex = index) }
    }

    fun addTag() {
        val name = _uiState.value.newTagName.trim()
        if (name.isEmpty()) return

        val allTags = _uiState.value.allTags
        val existingTag = allTags.find { it.name == name }

        if (existingTag != null) {
            if (existingTag.id in _uiState.value.selectedTagIds) {
                _uiState.update { it.copy(message = "标签「$name」已添加") }
                return
            }
            _uiState.update { state ->
                state.copy(
                    newTagName = "",
                    newTagColorIndex = 0,
                    selectedTagIds = state.selectedTagIds + existingTag.id
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                val color = TagColors.getOrElse(_uiState.value.newTagColorIndex) { TagColors[0] }
                val colorArgb = color.toIntArgb()
                val newTagId = shelfRepository.createBookTag(name, colorArgb)
                _uiState.update { state ->
                    state.copy(
                        newTagName = "",
                        newTagColorIndex = 0,
                        selectedTagIds = state.selectedTagIds + newTagId
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "创建标签失败: ${e.message}") }
            }
        }
    }

    fun saveTags() {
        val book = _uiState.value.book ?: return
        val allTags = _uiState.value.allTags
        val selectedNames = allTags.filter { it.id in _uiState.value.selectedTagIds }.map { it.name }
        val tagsString = selectedNames.joinToString("/")

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                shelfRepository.updateBookTags(book.id, tagsString)
                val updated = book.copy(tags = tagsString)
                _uiState.update {
                    it.copy(book = updated, isSaving = false, showTagEditor = false, message = "标签已保存")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, message = "保存失败: ${e.message}")
                }
            }
        }
    }

    fun showScoreEditor() {
        val book = _uiState.value.book ?: return
        val text = if (book.score == 0f) "" else String.format("%.1f", book.score)
        _uiState.update { it.copy(showScoreEditor = true, editScoreText = text) }
    }

    fun dismissScoreEditor() {
        _uiState.update { it.copy(showScoreEditor = false) }
    }

    fun onScoreTextChanged(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } > 1) return
        if (filtered.contains('.') && filtered.substringAfter('.').length > 1) return
        _uiState.update { it.copy(editScoreText = filtered) }
    }

    fun saveScore() {
        val book = _uiState.value.book ?: return
        val text = _uiState.value.editScoreText
        val score = text.toFloatOrNull()?.coerceIn(0f, 10f) ?: 0f

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                shelfRepository.updateBookScore(book.id, score)
                val updated = book.copy(score = score)
                _uiState.update {
                    it.copy(book = updated, isSaving = false, showScoreEditor = false, message = "评分已更新")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, message = "保存失败: ${e.message}")
                }
            }
        }
    }

    // ==================== 作者编辑 ====================

    fun showAuthorEditor() {
        val book = _uiState.value.book ?: return
        _uiState.update {
            it.copy(showAuthorEditor = true, editAuthorName = book.author ?: "")
        }
    }

    fun dismissAuthorEditor() {
        _uiState.update { it.copy(showAuthorEditor = false) }
    }

    fun onAuthorNameChanged(value: String) {
        _uiState.update { it.copy(editAuthorName = value) }
    }

    fun saveCover(imagePath: String) {
        val book = _uiState.value.book ?: return
        viewModelScope.launch {
            try {
                val srcFile = File(imagePath)
                val bookDir = File(book.folderPath)
                val coverFile = File(bookDir, "cover_${srcFile.name}")
                if (srcFile.absolutePath != coverFile.absolutePath) {
                    srcFile.copyTo(coverFile, overwrite = true)
                }
                _uiState.update {
                    it.copy(
                        book = book.copy(coverPath = coverFile.absolutePath),
                        showCoverPicker = false,
                        message = "封面已更新"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "封面设置失败: ${e.message}") }
            }
        }
    }

    fun saveAuthor() {
        val book = _uiState.value.book ?: return
        val newAuthor = _uiState.value.editAuthorName.trim().ifBlank { null }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                shelfRepository.updateBookAuthor(book.id, newAuthor)
                val updated = book.copy(author = newAuthor)
                _uiState.update {
                    it.copy(book = updated, isSaving = false, showAuthorEditor = false, message = "作者已更新")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, message = "修改失败: ${e.message}")
                }
            }
        }
    }
}