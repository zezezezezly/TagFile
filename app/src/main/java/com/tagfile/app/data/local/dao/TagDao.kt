package com.tagfile.app.data.local.dao

import androidx.room.*
import com.tagfile.app.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteById(tagId: Long)

    @Query("SELECT * FROM tags ORDER BY sort_order ASC, created_at DESC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY sort_order ASC, created_at DESC")
    suspend fun getAllList(): List<TagEntity>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getById(tagId: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getByIdFlow(tagId: Long): Flow<TagEntity?>

    @Query("SELECT DISTINCT group_name FROM tags WHERE group_name IS NOT NULL AND group_name != '' ORDER BY group_name ASC")
    suspend fun getAllGroups(): List<String>

    @Query("SELECT * FROM tags WHERE group_name = :groupName ORDER BY sort_order ASC, created_at DESC")
    suspend fun getByGroup(groupName: String): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>)

    @Query("DELETE FROM tags")
    suspend fun deleteAll()

    @Query("UPDATE tags SET group_name = :newName WHERE group_name = :oldName")
    suspend fun renameGroup(oldName: String, newName: String)

    @Query("UPDATE tags SET group_name = NULL WHERE group_name = :groupName")
    suspend fun clearGroup(groupName: String)

    @Query("UPDATE tags SET group_name = :toGroup WHERE group_name = :fromGroup")
    suspend fun mergeGroups(fromGroup: String, toGroup: String)
}
