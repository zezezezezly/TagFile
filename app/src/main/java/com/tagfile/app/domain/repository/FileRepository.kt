package com.tagfile.app.domain.repository

import com.tagfile.app.domain.model.FileItem

interface FileRepository {
    suspend fun getFiles(directoryPath: String): Result<List<FileItem>>
    suspend fun getFileInfo(filePath: String): Result<FileItem>
    suspend fun copyFile(sourcePath: String, destinationDir: String): Result<String>
    suspend fun copyFiles(sourcePaths: List<String>, destinationDir: String): Result<List<String>>
    suspend fun moveFile(sourcePath: String, destinationDir: String): Result<String>
    suspend fun moveFiles(sourcePaths: List<String>, destinationDir: String): Result<List<String>>
    suspend fun deleteFile(filePath: String): Result<Unit>
    suspend fun deleteFiles(filePaths: List<String>): Result<Unit>
    suspend fun renameFile(filePath: String, newName: String): Result<String>
    suspend fun createDirectory(parentPath: String, name: String): Result<String>
    suspend fun getStorageRoots(): List<String>
}
