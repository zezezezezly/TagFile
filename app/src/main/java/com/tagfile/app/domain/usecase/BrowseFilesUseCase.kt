package com.tagfile.app.domain.usecase

import com.tagfile.app.domain.model.FileItem
import com.tagfile.app.domain.repository.FileRepository
import com.tagfile.app.domain.repository.TagRepository
import javax.inject.Inject

class BrowseFilesUseCase @Inject constructor(
    private val fileRepository: FileRepository,
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(directoryPath: String): Result<List<FileItem>> {
        return fileRepository.getFiles(directoryPath).map { files ->
            files.map { file ->
                val tags = tagRepository.getTagsByFilePath(file.path)
                file.copy(tags = tags)
            }
        }
    }

    @Suppress("unused")
    suspend fun getFileInfo(filePath: String): Result<FileItem> {
        return fileRepository.getFileInfo(filePath).map { file ->
            val tags = tagRepository.getTagsByFilePath(file.path)
            file.copy(tags = tags)
        }
    }
}
