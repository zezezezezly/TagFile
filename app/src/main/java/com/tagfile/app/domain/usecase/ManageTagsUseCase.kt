package com.tagfile.app.domain.usecase

import com.tagfile.app.domain.model.Tag
import com.tagfile.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageTagsUseCase @Inject constructor(
    private val tagRepository: TagRepository
) {
    fun getAllTags(): Flow<List<Tag>> = tagRepository.getAllTags()

    @Suppress("unused")
    suspend fun getTagById(tagId: Long): Tag? = tagRepository.getTagById(tagId)

    suspend fun createTag(name: String, color: Int, icon: String? = null): Long {
        return tagRepository.createTag(name, color, icon)
    }

    suspend fun updateTag(tag: Tag) = tagRepository.updateTag(tag)

    suspend fun deleteTag(tagId: Long) = tagRepository.deleteTag(tagId)

    suspend fun addTagToFile(filePath: String, tagId: Long) {
        tagRepository.addTagToFile(filePath, tagId)
    }

    @Suppress("unused")
    suspend fun addTagToFiles(filePaths: List<String>, tagId: Long) {
        tagRepository.addTagToFiles(filePaths, tagId)
    }

    @Suppress("unused")
    suspend fun removeTagFromFile(filePath: String, tagId: Long) {
        tagRepository.removeTagFromFile(filePath, tagId)
    }

    suspend fun addTagToDirectory(directoryPath: String, tagId: Long) {
        tagRepository.addTagToDirectory(directoryPath, tagId)
    }

    suspend fun getTagFileCounts(): Map<Long, Int> {
        return tagRepository.getTagFileCounts()
    }
}
