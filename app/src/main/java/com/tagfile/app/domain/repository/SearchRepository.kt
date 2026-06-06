package com.tagfile.app.domain.repository

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.model.SearchFilter

interface SearchRepository {
    suspend fun searchFiles(filter: SearchFilter): Result<List<FileItem>>
    suspend fun getFilesByType(fileType: FileType): Result<List<FileItem>>
    suspend fun getRecentFiles(limit: Int = 50): Result<List<FileItem>>
    suspend fun getLargeFiles(minSizeBytes: Long = 50 * 1024 * 1024): Result<List<FileItem>>
    suspend fun getFileCountByExtensions(extensions: List<String>): Long
    suspend fun searchByType(fileType: FileType, limit: Int = 1000): Result<List<FileItem>>
}
