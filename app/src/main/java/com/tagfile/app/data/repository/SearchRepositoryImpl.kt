package com.tagfile.app.data.repository

import com.tagfile.app.data.filesystem.FileIndexer
import com.tagfile.app.data.filesystem.FileScanner
import com.tagfile.app.data.filesystem.FileSystemManager
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.entity.FileIndexEntity
import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.model.SearchFilter
import com.tagfile.app.domain.model.TagMode
import com.tagfile.app.domain.repository.SearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val fileSystemManager: FileSystemManager,
    private val fileScanner: FileScanner,
    private val fileTagDao: FileTagDao,
    private val fileIndexDao: FileIndexDao,
    @Suppress("unused") fileIndexer: FileIndexer
) : SearchRepository {

    companion object {
        private const val MAX_SEARCH_RESULTS = 500
        private const val MIN_INDEX_COUNT = 1000
    }

    override suspend fun searchFiles(filter: SearchFilter): Result<List<FileItem>> {
        return withContext(Dispatchers.IO) {
            try {
                if (filter.tagIds.isNotEmpty()) {
                    Result.success(searchByTags(filter))
                } else {
                    searchByIndexOrWalk(filter)
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getFilesByType(fileType: FileType): Result<List<FileItem>> {
        return fileScanner.scanForType(fileType)
    }

    override suspend fun getRecentFiles(limit: Int): Result<List<FileItem>> {
        return fileScanner.scanRecentFiles(limit)
    }

    override suspend fun getLargeFiles(minSizeBytes: Long): Result<List<FileItem>> {
        return fileScanner.scanLargeFiles(minSizeBytes)
    }

    override suspend fun getFileCountByExtensions(extensions: List<String>): Long {
        if (extensions.isEmpty()) return 0L
        return withContext(Dispatchers.IO) {
            fileIndexDao.countByExtensions(extensions)
        }
    }

    override suspend fun getUntaggedFileCount(): Long {
        return withContext(Dispatchers.IO) {
            fileIndexDao.count() - fileTagDao.countDistinctFilePaths()
        }
    }

    override suspend fun getUntaggedFiles(limit: Int): Result<List<FileItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val taggedPaths = fileTagDao.getAllDistinctFilePaths().toSet()
                val allFiles = fileIndexDao.getAllFilesByPath()
                val untagged = allFiles.filter { it.path !in taggedPaths }.take(limit)
                Result.success(untagged.map { it.toFileItem() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun searchByType(fileType: FileType, limit: Int): Result<List<FileItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val extensions = fileType.extensions
                if (extensions.isEmpty()) return@withContext Result.success(emptyList())
                val entities = fileIndexDao.searchAllByExtensions(extensions, limit)
                Result.success(entities.map { it.toFileItem() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun searchByIndexOrWalk(filter: SearchFilter): Result<List<FileItem>> {
        if (fileIndexDao.count() >= MIN_INDEX_COUNT) {
            return Result.success(applySizeDateFilters(queryIndex(filter), filter))
        }

        return Result.success(searchByParallelWalk(filter))
    }

    private suspend fun queryIndex(filter: SearchFilter): List<FileItem> {
        val keyword = filter.keyword.lowercase()
        val hasKeyword = keyword.isNotBlank()
        val hasType = filter.fileTypes.isNotEmpty()
        val dirOnly = filter.searchDirectories
        val limit = MAX_SEARCH_RESULTS

        val entities: List<FileIndexEntity> = when {
            hasKeyword && dirOnly && hasType -> {
                val extensions = filter.fileTypes.flatMap { it.extensions }
                val dirs = fileIndexDao.searchDirectoriesByName(keyword, limit)
                val files = if (extensions.isNotEmpty())
                    fileIndexDao.searchByNameAndExtensions(keyword, extensions, limit) else emptyList()
                (dirs + files).distinctBy { it.path }.take(limit)
            }
            hasKeyword && dirOnly -> fileIndexDao.searchDirectoriesByName(keyword, limit)
            hasKeyword && hasType -> {
                val extensions = filter.fileTypes.flatMap { it.extensions }
                if (extensions.isEmpty()) emptyList()
                else fileIndexDao.searchByNameAndExtensions(keyword, extensions, limit)
            }
            hasKeyword -> fileIndexDao.searchByName(keyword, limit)
            dirOnly && hasType -> {
                val extensions = filter.fileTypes.flatMap { it.extensions }
                val dirs = fileIndexDao.searchDirectories(limit)
                val files = if (extensions.isNotEmpty())
                    fileIndexDao.searchByExtensions(extensions, limit) else emptyList()
                (dirs + files).distinctBy { it.path }.take(limit)
            }
            hasType -> {
                val extensions = filter.fileTypes.flatMap { it.extensions }
                if (extensions.isEmpty()) emptyList()
                else fileIndexDao.searchByExtensions(extensions, limit)
            }
            dirOnly -> fileIndexDao.searchDirectories(limit)
            else -> emptyList()
        }

        return entities.map { it.toFileItem() }
    }

    private suspend fun searchByParallelWalk(filter: SearchFilter): List<FileItem> {
        val storageRoots = fileSystemManager.getStorageRoots()
        if (storageRoots.isEmpty()) return emptyList()

        val keyword = filter.keyword.lowercase()
        val hasKeyword = keyword.isNotBlank()
        val hasType = filter.fileTypes.isNotEmpty()
        val dirOnly = filter.searchDirectories
        val done = AtomicBoolean(false)
        val results = mutableListOf<FileItem>()

        coroutineScope {
            storageRoots.map { root ->
                async {
                    try {
                        File(root).walkTopDown().forEach { file ->
                            if (done.get()) return@async
                            yield()

                            if (!matchesFilterInline(file, filter, hasKeyword, keyword, hasType, dirOnly)) {
                                return@forEach
                            }

                            val info = fileSystemManager.getFileInfo(file.absolutePath) ?: return@forEach

                            synchronized(results) {
                                if (results.size < MAX_SEARCH_RESULTS) {
                                    results.add(info)
                                }
                                if (results.size >= MAX_SEARCH_RESULTS) {
                                    done.set(true)
                                }
                            }
                        }
                    } catch (_: Exception) { }
                }
            }.forEach { it.await() }
        }

        return applySizeDateFilters(results, filter)
    }

    private fun matchesFilterInline(
        file: File,
        filter: SearchFilter,
        hasKeyword: Boolean,
        keyword: String,
        hasType: Boolean,
        dirOnly: Boolean
    ): Boolean {
        if (file.isFile) {
            if (hasKeyword && !file.name.lowercase().contains(keyword)) return false
            if (dirOnly && !hasType) return false
            if (hasType) {
                val fileType = FileType.fromExtension(file.extension)
                if (fileType !in filter.fileTypes) return false
            }
            return true
        }

        if (file.isDirectory && file.name != "." && file.name != "..") {
            if (!dirOnly && !hasType && !hasKeyword) return false
            if (hasKeyword && !file.name.lowercase().contains(keyword)) return false
            return true
        }

        return false
    }

    private suspend fun searchByTags(filter: SearchFilter): List<FileItem> {
        val filePaths = when (filter.tagMode) {
            TagMode.AND -> fileTagDao.getFilePathsByTagIdsAnd(filter.tagIds, filter.tagIds.size)
            TagMode.OR -> fileTagDao.getFilePathsByTagIdsOr(filter.tagIds)
        }
        val files = filePaths
            .take(MAX_SEARCH_RESULTS)
            .mapNotNull { fileSystemManager.getFileInfo(it) }
        return applyFilters(files, filter)
    }

    private fun applyFilters(files: List<FileItem>, filter: SearchFilter): List<FileItem> {
        val dirOnly = filter.searchDirectories
        val hasType = filter.fileTypes.isNotEmpty()
        return files.filter { file ->
            (filter.keyword.isBlank() || file.name.contains(filter.keyword, ignoreCase = true)) &&
            (file.isDirectory || (!dirOnly && (!hasType || FileType.fromExtension(file.extension) in filter.fileTypes))) &&
            (filter.minSize == null || file.size >= filter.minSize) &&
            (filter.maxSize == null || file.size <= filter.maxSize) &&
            (filter.dateFrom == null || file.lastModified >= filter.dateFrom) &&
            (filter.dateTo == null || file.lastModified <= filter.dateTo)
        }
    }

    private fun applySizeDateFilters(files: List<FileItem>, filter: SearchFilter): List<FileItem> {
        return files.filter { file ->
            var matches = true
            if (filter.minSize != null && !file.isDirectory) {
                matches = matches && file.size >= filter.minSize
            }
            if (filter.maxSize != null && !file.isDirectory) {
                matches = matches && file.size <= filter.maxSize
            }
            if (filter.dateFrom != null) {
                matches = matches && file.lastModified >= filter.dateFrom
            }
            if (filter.dateTo != null) {
                matches = matches && file.lastModified <= filter.dateTo
            }
            matches
        }
    }

    private fun FileIndexEntity.toFileItem(): FileItem {
        return FileItem(
            path = path,
            name = name,
            isDirectory = isDirectory,
            size = size,
            lastModified = lastModified,
            extension = extension,
            mimeType = null
        )
    }
}
