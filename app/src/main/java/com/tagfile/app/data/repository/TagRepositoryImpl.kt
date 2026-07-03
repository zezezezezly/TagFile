package com.tagfile.app.data.repository

import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.dao.TagFileCount
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity
import com.tagfile.app.data.local.entity.TagEntity
import com.tagfile.app.data.mapper.EntityMapper.toDomain
import com.tagfile.app.data.mapper.EntityMapper.toEntity
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.domain.model.TagMode
import com.tagfile.app.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val fileTagDao: FileTagDao
) : TagRepository {

    override fun getAllTags(): Flow<List<Tag>> {
        return tagDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTagById(tagId: Long): Tag? {
        return tagDao.getById(tagId)?.toDomain()
    }

    override suspend fun getTagByIdFlow(tagId: Long): Flow<Tag?> {
        return tagDao.getByIdFlow(tagId).map { it?.toDomain() }
    }

    override suspend fun createTag(name: String, color: Int, icon: String?, groupName: String?): Long {
        val entity = TagEntity(name = name, color = color, icon = icon, groupName = groupName)
        return tagDao.insert(entity)
    }

    override suspend fun updateTag(tag: Tag) {
        tagDao.update(tag.toEntity())
    }

    override suspend fun deleteTag(tagId: Long) {
        fileTagDao.deleteAllCrossRefsByTagId(tagId)
        tagDao.deleteById(tagId)
    }

    override suspend fun addTagToFile(filePath: String, tagId: Long) {
        val crossRef = FileTagCrossRefEntity(filePath = filePath, tagId = tagId)
        fileTagDao.insertCrossRef(crossRef)
    }

    override suspend fun addTagToFiles(filePaths: List<String>, tagId: Long) {
        val crossRefs = filePaths.map { FileTagCrossRefEntity(filePath = it, tagId = tagId) }
        fileTagDao.insertCrossRefs(crossRefs)
    }

    override suspend fun removeTagFromFile(filePath: String, tagId: Long) {
        fileTagDao.deleteCrossRefByFileAndTag(filePath, tagId)
    }

    override suspend fun addTagToDirectory(directoryPath: String, tagId: Long) {
        addTagToFile(directoryPath, tagId)
    }

    override suspend fun getTagsByFilePath(filePath: String): List<Tag> {
        val tagIds = fileTagDao.getTagIdsByFilePath(filePath)
        return tagIds.mapNotNull { tagDao.getById(it)?.toDomain() }
    }

    override suspend fun getFilePathsByTagId(tagId: Long): List<String> {
        return fileTagDao.getFilePathsByTagId(tagId)
    }

    override suspend fun getFilePathsByTagIds(tagIds: List<Long>, mode: TagMode): List<String> {
        return when (mode) {
            TagMode.AND -> fileTagDao.getFilePathsByTagIdsAnd(tagIds, tagIds.size)
            TagMode.OR -> fileTagDao.getFilePathsByTagIdsOr(tagIds)
        }
    }

    override suspend fun updateFilePath(oldPath: String, newPath: String) {
        fileTagDao.updateFilePath(oldPath, newPath)
    }

    override suspend fun deleteAllCrossRefsByFilePath(filePath: String) {
        fileTagDao.deleteAllCrossRefsByFilePath(filePath)
    }

    override suspend fun getTagFileCounts(): Map<Long, Int> {
        return fileTagDao.getTagFileCounts().associate { it.tagId to it.count }
    }

    override suspend fun getAllGroups(): List<String> {
        return tagDao.getAllGroups()
    }

    override suspend fun renameGroup(oldName: String, newName: String) {
        tagDao.renameGroup(oldName, newName)
    }

    override suspend fun clearGroup(groupName: String) {
        tagDao.clearGroup(groupName)
    }

    override suspend fun mergeGroups(fromGroup: String, toGroup: String) {
        tagDao.mergeGroups(fromGroup, toGroup)
    }
}
