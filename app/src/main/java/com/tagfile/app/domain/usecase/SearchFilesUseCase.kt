package com.tagfile.app.domain.usecase

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.model.FileType
import com.tagfile.app.domain.model.SearchFilter
import com.tagfile.app.domain.repository.SearchRepository
import com.tagfile.app.domain.repository.TagRepository
import javax.inject.Inject

class SearchFilesUseCase @Inject constructor(
    private val searchRepository: SearchRepository,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(filter: SearchFilter): Result<List<FileItem>> {
        return searchRepository.searchFiles(filter).map { files ->
            files.map { file ->
                val tags = tagRepository.getTagsByFilePath(file.path)
                file.copy(tags = tags)
            }
        }
    }

    suspend fun getFilesByType(fileType: FileType): Result<List<FileItem>> {
        return searchRepository.getFilesByType(fileType).map { files ->
            files.map { file ->
                val tags = tagRepository.getTagsByFilePath(file.path)
                file.copy(tags = tags)
            }
        }
    }

    suspend fun getRecentFiles(limit: Int = 50): Result<List<FileItem>> {
        return searchRepository.getRecentFiles(limit).map { files ->
            files.map { file ->
                val tags = tagRepository.getTagsByFilePath(file.path)
                file.copy(tags = tags)
            }
        }
    }

    suspend fun getLargeFiles(minSizeBytes: Long = 50 * 1024 * 1024): Result<List<FileItem>> {
        return searchRepository.getLargeFiles(minSizeBytes).map { files ->
            files.map { file ->
                val tags = tagRepository.getTagsByFilePath(file.path)
                file.copy(tags = tags)
            }
        }
    }
}
