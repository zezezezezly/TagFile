package com.tagfile.app.data.local.dao

import androidx.room.*
import com.tagfile.app.data.local.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: BookEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<BookEntity>)

    @Update
    suspend fun update(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM books ORDER BY last_read_time DESC")
    fun getAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY last_read_time DESC")
    suspend fun getAllList(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE folder_path = :folderPath LIMIT 1")
    suspend fun getByFolderPath(folderPath: String): BookEntity?

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' ORDER BY last_read_time DESC")
    fun searchByTitle(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE author LIKE '%' || :query || '%' ORDER BY last_read_time DESC")
    fun searchByAuthor(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE tags LIKE '%' || :query || '%' ORDER BY last_read_time DESC")
    fun searchByTags(query: String): Flow<List<BookEntity>>

    @Query("UPDATE books SET view_count = view_count + 1, last_read_time = :time WHERE id = :id")
    suspend fun incrementViewCount(id: Long, time: Long)

    @Query("UPDATE books SET read_duration = read_duration + :duration, last_read_time = :time WHERE id = :id")
    suspend fun addReadDuration(id: Long, duration: Long, time: Long)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun count(): Int

    @Query("DELETE FROM books")
    suspend fun deleteAll()

    @Query("UPDATE books SET description = :description WHERE id = :id")
    suspend fun updateDescription(id: Long, description: String)

    @Query("SELECT * FROM books ORDER BY RANDOM() LIMIT :limit")
    fun getRandomBooks(limit: Int): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE last_read_time > 0 ORDER BY last_read_time DESC")
    fun getReadHistory(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE last_read_time > 0 ORDER BY last_read_time DESC LIMIT :limit")
    fun getRecentlyRead(limit: Int): Flow<List<BookEntity>>

    @Query("SELECT id FROM books")
    suspend fun getAllIds(): List<Long>

    @Query("UPDATE books SET tags = :tags WHERE id = :id")
    suspend fun updateTags(id: Long, tags: String)

    @Query("UPDATE books SET score = :score WHERE id = :id")
    suspend fun updateScore(id: Long, score: Float)

    @Query("SELECT * FROM books WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<BookEntity>

    @Query("UPDATE books SET current_page = :page WHERE id = :id")
    suspend fun updateCurrentPage(id: Long, page: Int)
}
