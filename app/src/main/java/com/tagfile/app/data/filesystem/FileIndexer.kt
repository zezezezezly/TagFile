package com.tagfile.app.data.filesystem

import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.entity.FileIndexEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileIndexer @Inject constructor(
    private val fileSystemManager: FileSystemManager,
    private val fileIndexDao: FileIndexDao
) {
    companion object {
        private const val BATCH_SIZE = 2000
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _indexedCount = MutableStateFlow(0L)
    val indexedCount: StateFlow<Long> = _indexedCount.asStateFlow()

    private val _isIndexing = MutableStateFlow(false)
    val isIndexing: StateFlow<Boolean> = _isIndexing.asStateFlow()

    private var indexJob: Job? = null

    init {
        startFullIndex()
    }

    fun startFullIndex() {
        if (_isIndexing.value) return
        indexJob?.cancel()
        indexJob = scope.launch {
            _isIndexing.value = true
            try {
                fileIndexDao.deleteAll()
                _indexedCount.value = 0

                val storageRoots = fileSystemManager.getStorageRoots()

                coroutineScope {
                    storageRoots.map { root ->
                        async {
                            indexRoot(root)
                        }
                    }
                }

                _indexedCount.value = fileIndexDao.count()
            } catch (_: CancellationException) {
            } finally {
                _isIndexing.value = false
            }
        }
    }

    private suspend fun indexRoot(rootPath: String) {
        val batch = mutableListOf<FileIndexEntity>()
        try {
            File(rootPath).walkTopDown().forEach { file ->
                yield()
                val entity = file.toIndexEntity() ?: return@forEach
                batch.add(entity)
                if (batch.size >= BATCH_SIZE) {
                    fileIndexDao.insertAll(batch.toList())
                    _indexedCount.value += batch.size
                    batch.clear()
                }
            }
            if (batch.isNotEmpty()) {
                fileIndexDao.insertAll(batch.toList())
                _indexedCount.value += batch.size
            }
        } catch (_: Exception) { }
    }

    private fun File.toIndexEntity(): FileIndexEntity? {
        if (!isFile && !isDirectory) return null
        val fileName = name
        if (fileName.isNullOrEmpty() || fileName == "." || fileName == "..") return null
        if (fileName.startsWith(".")) return null
        return FileIndexEntity(
            path = absolutePath,
            name = fileName,
            nameLower = fileName.lowercase(),
            isDirectory = isDirectory,
            extension = if (isFile) extension else "",
            size = if (isDirectory) 0L else length(),
            lastModified = lastModified()
        )
    }
}
