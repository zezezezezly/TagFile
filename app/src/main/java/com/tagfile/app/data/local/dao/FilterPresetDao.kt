package com.tagfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tagfile.app.data.local.entity.FilterPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterPresetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FilterPresetEntity): Long

    @Update
    suspend fun update(entity: FilterPresetEntity)

    @Delete
    suspend fun delete(entity: FilterPresetEntity)

    @Query("SELECT * FROM filter_presets ORDER BY created_at DESC")
    fun getAll(): Flow<List<FilterPresetEntity>>

    @Query("SELECT * FROM filter_presets ORDER BY created_at DESC")
    suspend fun getAllList(): List<FilterPresetEntity>

    @Query("SELECT * FROM filter_presets WHERE id = :id")
    suspend fun getById(id: Long): FilterPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FilterPresetEntity>)

    @Query("DELETE FROM filter_presets")
    suspend fun deleteAll()
}
