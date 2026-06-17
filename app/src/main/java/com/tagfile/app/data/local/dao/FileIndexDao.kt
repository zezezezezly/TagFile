package com.tagfile.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tagfile.app.data.local.entity.FileIndexEntity

@Dao
interface FileIndexDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<FileIndexEntity>)

    @Query("DELETE FROM file_index")
    suspend fun deleteAll()

    @Query("SELECT * FROM file_index")
    suspend fun getAllList(): List<FileIndexEntity>

    @Query("SELECT COUNT(*) FROM file_index")
    suspend fun count(): Long

    @Query("SELECT * FROM file_index WHERE name_lower LIKE '%' || :keyword || '%' LIMIT :limit")
    suspend fun searchByName(keyword: String, limit: Int): List<FileIndexEntity>

    @Query("""
        SELECT * FROM file_index 
        WHERE is_directory = 1 AND name_lower LIKE '%' || :keyword || '%' 
        LIMIT :limit
    """)
    suspend fun searchDirectoriesByName(keyword: String, limit: Int): List<FileIndexEntity>

    @Query("""
        SELECT * FROM file_index 
        WHERE extension IN (:extensions) AND name_lower LIKE '%' || :keyword || '%' 
        LIMIT :limit
    """)
    suspend fun searchByNameAndExtensions(keyword: String, extensions: List<String>, limit: Int): List<FileIndexEntity>

    @Query("SELECT * FROM file_index WHERE extension IN (:extensions) LIMIT :limit")
    suspend fun searchByExtensions(extensions: List<String>, limit: Int): List<FileIndexEntity>

    @Query("SELECT * FROM file_index WHERE is_directory = 1 LIMIT :limit")
    suspend fun searchDirectories(limit: Int): List<FileIndexEntity>

    @Query("SELECT COUNT(*) FROM file_index WHERE extension IN (:extensions)")
    suspend fun countByExtensions(extensions: List<String>): Long

    @Query("SELECT * FROM file_index WHERE extension IN (:extensions) ORDER BY name ASC LIMIT :limit")
    suspend fun searchAllByExtensions(extensions: List<String>, limit: Int): List<FileIndexEntity>

    @Query("SELECT * FROM file_index WHERE is_directory = 0")
    suspend fun getAllFilesByPath(): List<FileIndexEntity>
}
