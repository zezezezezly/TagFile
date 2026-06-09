package com.tagfile.app.domain.repository

import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.model.RepairResult
import kotlinx.coroutines.flow.Flow

enum class SearchMode { TITLE, AUTHOR, TAGS }

interface ShelfRepository {
    fun getAllBooks(): Flow<List<Book>>
    suspend fun scanAndAddBooks(folderPath: String): List<Book>
    suspend fun getBookById(id: Long): Book?
    suspend fun incrementViewCount(id: Long)
    suspend fun addDuration(id: Long, duration: Long)
    fun searchBooks(query: String, mode: SearchMode): Flow<List<Book>>
    suspend fun getDailyRecommendations(count: Int, seed: Long): List<Book>
    fun getImagesInBook(book: Book): List<String>
    fun getRecentlyReadBooks(limit: Int): Flow<List<Book>>
    suspend fun updateBookDescription(id: Long, description: String)
    suspend fun updateBookScore(id: Long, score: Float)
    suspend fun updateBookTags(id: Long, tags: String)
    suspend fun updateBookAuthor(id: Long, author: String?)
    suspend fun createBookTag(name: String, color: Int): Long
    fun getAllBookTags(): Flow<List<com.tagfile.app.domain.model.Tag>>
    suspend fun repairBookData(): List<RepairResult>
}
