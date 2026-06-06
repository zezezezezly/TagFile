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

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getById(tagId: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getByIdFlow(tagId: Long): Flow<TagEntity?>
}
