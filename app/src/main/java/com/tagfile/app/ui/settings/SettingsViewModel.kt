package com.tagfile.app.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tagfile.app.data.export.DatabaseExport
import com.tagfile.app.data.export.BookJson
import com.tagfile.app.data.export.TagJson
import com.tagfile.app.data.export.FileIndexJson
import com.tagfile.app.data.export.FileTagCrossRefJson
import com.tagfile.app.data.export.FilterPresetJson
import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.dao.FilterPresetDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.entity.BookEntity
import com.tagfile.app.data.local.entity.FileIndexEntity
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity
import com.tagfile.app.data.local.entity.FilterPresetEntity
import com.tagfile.app.data.local.entity.TagEntity
import com.tagfile.app.data.preferences.AppearancePreferences
import com.tagfile.app.data.preferences.EnhancePreferences
import com.tagfile.app.data.preferences.ShelfPreferences
import com.tagfile.app.domain.repository.ShelfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val bookDao: BookDao,
    private val tagDao: TagDao,
    private val fileIndexDao: FileIndexDao,
    private val fileTagDao: FileTagDao,
    private val filterPresetDao: FilterPresetDao,
    private val shelfRepository: ShelfRepository,
    private val appearancePreferences: AppearancePreferences,
    private val enhancePreferences: EnhancePreferences,
    private val shelfPreferences: ShelfPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkMode = appearancePreferences.isDarkMode.value,
            isContinuousEnhance = enhancePreferences.continuousEnhance.value,
            shelfFolderPath = shelfPreferences.shelfFolderPath.value
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var pendingImportContent: String? = null

    init {
        viewModelScope.launch {
            shelfPreferences.shelfFolderPath.collect { path ->
                _uiState.update { it.copy(shelfFolderPath = path) }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ToggleDarkMode -> {
                val newValue = !_uiState.value.isDarkMode
                _uiState.update { it.copy(isDarkMode = newValue) }
                appearancePreferences.setDarkMode(newValue)
            }
            is SettingsEvent.ToggleContinuousEnhance -> {
                val newValue = !_uiState.value.isContinuousEnhance
                _uiState.update { it.copy(isContinuousEnhance = newValue) }
                enhancePreferences.setContinuousEnhance(newValue)
            }
            is SettingsEvent.UpdateShelfFolderPath -> {
                _uiState.update { it.copy(shelfFolderPath = event.path) }
                shelfPreferences.setShelfFolderPath(event.path)
            }
            is SettingsEvent.ClearMessage -> _uiState.update { it.copy(message = null) }
            is SettingsEvent.ResetShelfDatabase -> resetShelfDatabase()
            is SettingsEvent.ScanShelfBooks -> scanShelfBooks()

            is SettingsEvent.ExportDatabase -> exportDatabase()
            is SettingsEvent.ImportDatabase -> triggerImport()
            is SettingsEvent.DismissImportModeDialog ->
                _uiState.update { it.copy(showImportModeDialog = false) }
            is SettingsEvent.ConfirmImportMode -> {
                pendingImportContent?.let { content ->
                    importDatabase(content, event.isReplace)
                }
            }
        }
    }

    // ==================== 导出 ====================

    fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val json = withContext(Dispatchers.IO) { buildExportJson() }
                withContext(Dispatchers.IO) {
                    appContext.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                _uiState.update { it.copy(isExporting = false, message = "数据已导出") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, message = "导出失败: ${e.message}") }
            }
        }
    }

    private fun exportDatabase() {
        _uiState.update { it.copy(message = "请选择导出位置") }
    }

    private suspend fun buildExportJson(): String {
        val books = bookDao.getAllList().map {
            BookJson(it.id, it.title, it.author, it.tags, it.coverPath, it.folderPath,
                it.pageCount, it.viewCount, it.totalDuration, it.description, it.score,
                it.lastReadTime, it.createdAt)
        }
        val tags = tagDao.getAllList().map {
            TagJson(it.id, it.name, it.color, it.icon, it.sortOrder, it.createdAt)
        }
        val fileIndex = fileIndexDao.getAllList().map {
            FileIndexJson(it.path, it.name, it.nameLower, it.isDirectory, it.extension, it.size, it.lastModified)
        }
        val crossRefs = fileTagDao.getAllList().map {
            FileTagCrossRefJson(it.filePath, it.tagId, it.isInherited)
        }
        val presets = filterPresetDao.getAllList().map {
            FilterPresetJson(it.id, it.name, it.strength, it.sharpness, it.denoise,
                it.lineDarkening, it.contrast, it.saturation, it.upscaleFactor, it.createdAt)
        }
        return Json { prettyPrint = true }.encodeToString(
            DatabaseExport.serializer(),
            DatabaseExport(books = books, tags = tags, fileIndex = fileIndex,
                fileTagCrossRefs = crossRefs, filterPresets = presets)
        )
    }

    // ==================== 导入 ====================

    private fun triggerImport() {
        _uiState.update { it.copy(message = "请选择要导入的 JSON 文件") }
    }

    fun readImportFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
                        ?: throw Exception("无法读取文件")
                }
                pendingImportContent = content
                _uiState.update { it.copy(showImportModeDialog = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "读取文件失败: ${e.message}") }
            }
        }
    }

    private fun importDatabase(jsonContent: String, isReplace: Boolean) {
        pendingImportContent = null
        _uiState.update { it.copy(isImporting = true, showImportModeDialog = false) }
        viewModelScope.launch {
            try {
                val export = withContext(Dispatchers.IO) {
                    Json.decodeFromString(DatabaseExport.serializer(), jsonContent)
                }
                withContext(Dispatchers.IO) {
                    if (isReplace) {
                        // 全量替换：按依赖关系反向删除，再插入
                        fileTagDao.deleteAll()
                        tagDao.deleteAll()
                        fileIndexDao.deleteAll()
                        bookDao.deleteAll()
                        filterPresetDao.deleteAll()
                    }
                    // 插入（replace 模式全覆盖，merge 模式 INSERT OR REPLACE 按主键覆盖）
                    bookDao.insertAll(export.books.map {
                        BookEntity(it.id, it.title, it.author, it.tags, it.coverPath,
                            it.folderPath, it.pageCount, it.viewCount, it.totalDuration,
                            it.description, it.score, it.lastReadTime, it.createdAt)
                    })
                    tagDao.insertAll(export.tags.map {
                        TagEntity(it.id, it.name, it.color, it.icon, it.sortOrder, it.createdAt)
                    })
                    fileIndexDao.insertAll(export.fileIndex.map {
                        FileIndexEntity(it.path, it.name, it.nameLower, it.isDirectory,
                            it.extension, it.size, it.lastModified)
                    })
                    fileTagDao.insertCrossRefs(export.fileTagCrossRefs.map {
                        FileTagCrossRefEntity(it.filePath, it.tagId, it.isInherited)
                    })
                    filterPresetDao.insertAll(export.filterPresets.map {
                        FilterPresetEntity(it.id, it.name, it.strength, it.sharpness, it.denoise,
                            it.lineDarkening, it.contrast, it.saturation, it.upscaleFactor, it.createdAt)
                    })
                }
                val mode = if (isReplace) "替换" else "合并"
                _uiState.update { it.copy(isImporting = false, message = "数据已导入（${mode}模式）") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, message = "导入失败: ${e.message}") }
            }
        }
    }

    // ==================== 其他 ====================

    @Suppress("unused")
    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    private fun resetShelfDatabase() {
        viewModelScope.launch {
            _uiState.update { it.copy(isResettingShelf = true) }
            try {
                bookDao.deleteAll()
                _uiState.update { it.copy(isResettingShelf = false, message = "书架数据库已重置") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isResettingShelf = false, message = "重置失败: ${e.message}") }
            }
        }
    }

    private fun scanShelfBooks() {
        val path = _uiState.value.shelfFolderPath ?: return
        _uiState.update { it.copy(isScanningShelf = true) }
        viewModelScope.launch {
            try {
                val newBooks = shelfRepository.scanAndAddBooks(path)
                _uiState.update {
                    it.copy(isScanningShelf = false, message = "已添加 ${newBooks.size} 本新书")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isScanningShelf = false, message = "扫描失败: ${e.message}")
                }
            }
        }
    }
}