package com.tagfile.app.data.local.dao

import androidx.room.*
import com.tagfile.app.data.local.entity.TrashEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrashDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TrashEntity): Long

    @Query("SELECT * FROM trash ORDER BY deleted_at DESC")
    fun getAll(): Flow<List<TrashEntity>>

    @Query("SELECT * FROM trash ORDER BY deleted_at DESC")
    suspend fun getAllList(): List<TrashEntity>

    @Query("SELECT * FROM trash WHERE id = :id")
    suspend fun getById(id: Long): TrashEntity?

    @Query("DELETE FROM trash WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM trash WHERE deleted_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM trash")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TrashEntity>)

    @Query("DELETE FROM trash")
    suspend fun deleteAll()
}