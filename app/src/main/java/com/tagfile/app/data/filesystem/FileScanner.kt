package com.tagfile.app.data.filesystem

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileScanner @Inject constructor(
    private val fileSystemManager: FileSystemManager
) {
    suspend fun scanForType(fileType: FileType): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<FileItem>()
            val storageRoots = fileSystemManager.getStorageRoots()

            for (root in storageRoots) {
                scanDirectoryForType(File(root), fileType, results)
            }

            Result.success(results.sortedByDescending { it.lastModified })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanRecentFiles(limit: Int = 50): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<FileItem>()
            val storageRoots = fileSystemManager.getStorageRoots()

            for (root in storageRoots) {
                scanDirectoryRecent(File(root), results)
            }

            Result.success(
                results
                    .sortedByDescending { it.lastModified }
                    .take(limit)
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanLargeFiles(minSizeBytes: Long): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<FileItem>()
            val storageRoots = fileSystemManager.getStorageRoots()

            for (root in storageRoots) {
                scanDirectoryLarge(File(root), minSizeBytes, results)
            }

            Result.success(results.sortedByDescending { it.size })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scanDirectoryForType(dir: File, fileType: FileType, results: MutableList<FileItem>) {
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory && !file.name.startsWith(".")) {
                    scanDirectoryForType(file, fileType, results)
                } else if (file.isFile) {
                    val ext = file.extension.lowercase()
                    if (ext in fileType.extensions) {
                        results.add(file.toFileItem())
                    }
                }
            }
        } catch (_: Exception) { }
    }

    private fun scanDirectoryRecent(dir: File, results: MutableList<FileItem>) {
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory && !file.name.startsWith(".")) {
                    scanDirectoryRecent(file, results)
                } else if (file.isFile) {
                    results.add(file.toFileItem())
                }
            }
        } catch (_: Exception) { }
    }

    private fun scanDirectoryLarge(dir: File, minSize: Long, results: MutableList<FileItem>) {
        try {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory && !file.name.startsWith(".")) {
                    scanDirectoryLarge(file, minSize, results)
                } else if (file.isFile && file.length() >= minSize) {
                    results.add(file.toFileItem())
                }
            }
        } catch (_: Exception) { }
    }

    private fun File.toFileItem(): FileItem {
        return FileItem(
            path = absolutePath,
            name = name,
            isDirectory = isDirectory,
            size = if (isDirectory) 0L else length(),
            lastModified = lastModified(),
            extension = extension,
            mimeType = if (isFile) fileSystemManager.getMimeType(absolutePath) else null
        )
    }
}
