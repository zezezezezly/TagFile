package com.tagfile.app.domain.usecase

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.repository.FileRepository
import com.tagfile.app.domain.repository.TagRepository
import javax.inject.Inject

class FileOperationsUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val tagRepository: TagRepository
) {
    @Suppress("unused")
    suspend fun copyFile(sourcePath: String, destinationDir: String): Result<String> {
        return fileRepository.copyFile(sourcePath, destinationDir)
    }

    suspend fun copyFiles(sourcePaths: List<String>, destinationDir: String): Result<List<String>> {
        return fileRepository.copyFiles(sourcePaths, destinationDir)
    }

    @Suppress("unused")
    suspend fun moveFile(sourcePath: String, destinationDir: String): Result<String> {
        return fileRepository.moveFile(sourcePath, destinationDir).also { result ->
            result.onSuccess { newPath ->
                tagRepository.updateFilePath(sourcePath, newPath)
            }
        }
    }

    suspend fun moveFiles(sourcePaths: List<String>, destinationDir: String): Result<List<String>> {
        return fileRepository.moveFiles(sourcePaths, destinationDir).also { result ->
            result.onSuccess { newPaths ->
                sourcePaths.zip(newPaths).forEach { (old, new) ->
                    tagRepository.updateFilePath(old, new)
                }
            }
        }
    }

    @Suppress("unused")
    suspend fun deleteFile(filePath: String): Result<Unit> {
        return fileRepository.deleteFile(filePath).also { result ->
            result.onSuccess {
                tagRepository.deleteAllCrossRefsByFilePath(filePath)
            }
        }
    }

    suspend fun deleteFiles(filePaths: List<String>): Result<Unit> {
        return fileRepository.deleteFiles(filePaths).also { result ->
            result.onSuccess {
                filePaths.forEach { path ->
                    tagRepository.deleteAllCrossRefsByFilePath(path)
                }
            }
        }
    }

    suspend fun renameFile(filePath: String, newName: String): Result<String> {
        return fileRepository.renameFile(filePath, newName).also { result ->
            result.onSuccess { newPath ->
                tagRepository.updateFilePath(filePath, newPath)
            }
        }
    }

    suspend fun createDirectory(parentPath: String, name: String): Result<String> {
        return fileRepository.createDirectory(parentPath, name)
    }

    @Suppress("unused")
    suspend fun getFileInfo(filePath: String): Result<FileItem> {
        return fileRepository.getFileInfo(filePath)
    }

    @Suppress("unused")
    suspend fun getStorageRoots(): List<String> {
        return fileRepository.getStorageRoots()
    }
}
