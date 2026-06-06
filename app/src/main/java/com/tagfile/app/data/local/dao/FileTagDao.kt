package com.tagfile.app.data.local.dao

import androidx.room.*
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity

@Dao
interface FileTagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: FileTagCrossRefEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<FileTagCrossRefEntity>)

    @Delete
    suspend fun deleteCrossRef(crossRef: FileTagCrossRefEntity)

    @Query("DELETE FROM file_tag_cross_ref WHERE file_path = :filePath AND tag_id = :tagId")
    suspend fun deleteCrossRefByFileAndTag(filePath: String, tagId: Long)

    @Query("DELETE FROM file_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun deleteAllCrossRefsByTagId(tagId: Long)

    @Query("DELETE FROM file_tag_cross_ref WHERE file_path = :filePath")
    suspend fun deleteAllCrossRefsByFilePath(filePath: String)

    @Query("SELECT file_path FROM file_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun getFilePathsByTagId(tagId: Long): List<String>

    @Query("SELECT * FROM file_tag_cross_ref WHERE file_path = :filePath")
    suspend fun getCrossRefsByFilePath(filePath: String): List<FileTagCrossRefEntity>

    @Query("SELECT tag_id FROM file_tag_cross_ref WHERE file_path = :filePath")
    suspend fun getTagIdsByFilePath(filePath: String): List<Long>

    @Query("""
        SELECT file_path FROM file_tag_cross_ref 
        WHERE tag_id IN (:tagIds) 
        GROUP BY file_path 
        HAVING COUNT(DISTINCT tag_id) = :tagCount
    """)
    suspend fun getFilePathsByTagIdsAnd(tagIds: List<Long>, tagCount: Int): List<String>

    @Query("""
        SELECT DISTINCT file_path FROM file_tag_cross_ref 
        WHERE tag_id IN (:tagIds)
    """)
    suspend fun getFilePathsByTagIdsOr(tagIds: List<Long>): List<String>

    @Query("UPDATE file_tag_cross_ref SET file_path = :newPath WHERE file_path = :oldPath")
    suspend fun updateFilePath(oldPath: String, newPath: String)

    @Query("DELETE FROM file_tag_cross_ref WHERE file_path LIKE :pathPrefix || '%'")
    suspend fun deleteCrossRefsByPathPrefix(pathPrefix: String)

    @Query("SELECT file_path FROM file_tag_cross_ref")
    suspend fun getAllTaggedFilePaths(): List<String>

    @Query("SELECT tag_id AS tagId, COUNT(DISTINCT file_path) AS count FROM file_tag_cross_ref GROUP BY tag_id")
    suspend fun getTagFileCounts(): List<TagFileCount>
}

data class TagFileCount(
    val tagId: Long,
    val count: Int
)
