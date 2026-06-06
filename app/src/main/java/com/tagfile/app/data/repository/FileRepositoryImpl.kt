package com.tagfile.app.data.repository

import com.tagfile.app.data.filesystem.FileSystemManager
import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.repository.FileRepository
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    private val fileSystemManager: FileSystemManager
) : FileRepository {

    override suspend fun getFiles(directoryPath: String): Result<List<FileItem>> {
        return try {
            Result.success(fileSystemManager.getFiles(directoryPath))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFileInfo(filePath: String): Result<FileItem> {
        return try {
            val item = fileSystemManager.getFileInfo(filePath)
            if (item != null) Result.success(item)
            else Result.failure(Exception("文件不存在"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun copyFile(sourcePath: String, destinationDir: String): Result<String> {
        return try {
            Result.success(fileSystemManager.copyFile(sourcePath, destinationDir))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun copyFiles(sourcePaths: List<String>, destinationDir: String): Result<List<String>> {
        return try {
            val results = sourcePaths.map { fileSystemManager.copyFile(it, destinationDir) }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveFile(sourcePath: String, destinationDir: String): Result<String> {
        return try {
            Result.success(fileSystemManager.moveFile(sourcePath, destinationDir))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun moveFiles(sourcePaths: List<String>, destinationDir: String): Result<List<String>> {
        return try {
            val results = sourcePaths.map { fileSystemManager.moveFile(it, destinationDir) }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFile(filePath: String): Result<Unit> {
        return try {
            fileSystemManager.moveToTrash(filePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFiles(filePaths: List<String>): Result<Unit> {
        return try {
            filePaths.forEach { fileSystemManager.moveToTrash(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun renameFile(filePath: String, newName: String): Result<String> {
        return try {
            Result.success(fileSystemManager.renameFile(filePath, newName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createDirectory(parentPath: String, name: String): Result<String> {
        return try {
            Result.success(fileSystemManager.createDirectory(parentPath, name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStorageRoots(): List<String> {
        return fileSystemManager.getStorageRoots()
    }
}
