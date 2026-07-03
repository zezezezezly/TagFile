package com.tagfile.app.domain.repository

import com.tagfile.app.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllTags(): Flow<List<Tag>>
    suspend fun getTagById(tagId: Long): Tag?
    suspend fun getTagByIdFlow(tagId: Long): Flow<Tag?>
    suspend fun createTag(name: String, color: Int, icon: String? = null, groupName: String? = null): Long
    suspend fun updateTag(tag: Tag)
    suspend fun deleteTag(tagId: Long)
    suspend fun addTagToFile(filePath: String, tagId: Long)
    suspend fun addTagToFiles(filePaths: List<String>, tagId: Long)
    suspend fun removeTagFromFile(filePath: String, tagId: Long)
    suspend fun addTagToDirectory(directoryPath: String, tagId: Long)
    suspend fun getTagsByFilePath(filePath: String): List<Tag>
    suspend fun getFilePathsByTagId(tagId: Long): List<String>
    suspend fun getFilePathsByTagIds(tagIds: List<Long>, mode: com.tagfile.app.domain.model.TagMode): List<String>
    suspend fun updateFilePath(oldPath: String, newPath: String)
    suspend fun deleteAllCrossRefsByFilePath(filePath: String)
    suspend fun getTagFileCounts(): Map<Long, Int>
    suspend fun getAllGroups(): List<String>
    suspend fun renameGroup(oldName: String, newName: String)
    suspend fun clearGroup(groupName: String)
    suspend fun mergeGroups(fromGroup: String, toGroup: String)
}
