package com.tagfile.app.data.repository

import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.entity.BookEntity
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity
import com.tagfile.app.data.local.entity.TagEntity
import com.tagfile.app.data.mapper.EntityMapper.toDomain
import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.model.Tag
import com.tagfile.app.domain.repository.SearchMode
import com.tagfile.app.domain.repository.ShelfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelfRepositoryImpl @Inject constructor(
    private val bookDao: BookDao,
    private val fileTagDao: FileTagDao,
    private val tagDao: TagDao
) : ShelfRepository {

    override fun getAllBooks(): Flow<List<Book>> {
        return bookDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getBookById(id: Long): Book? {
        return bookDao.getById(id)?.toDomain()
    }

    override suspend fun scanAndAddBooks(folderPath: String): List<Book> {
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
        val folder = File(resolvePath(folderPath))
        if (!folder.exists() || !folder.isDirectory) return emptyList()

        val newBooks = mutableListOf<Book>()
        val scannedPaths = mutableSetOf<String>()
        scanDirectoryRecursive(folder, imageExtensions, newBooks, scannedPaths, null)

        val existingBooks = bookDao.getAll().firstOrNull() ?: emptyList()
        for (book in existingBooks) {
            val resolvedPath = resolvePath(book.folderPath)
            if (resolvedPath !in scannedPaths || !File(resolvedPath).exists()) {
                bookDao.deleteById(book.id)
            }
        }

        return newBooks
    }

    private suspend fun scanDirectoryRecursive(
        dir: File,
        imageExtensions: Set<String>,
        newBooks: MutableList<Book>,
        scannedPaths: MutableSet<String>,
        inheritedAuthor: String?
    ) {
        val subDirs = dir.listFiles { f -> f.isDirectory } ?: return

        for (subDir in subDirs) {
            val existing = bookDao.getByFolderPath(subDir.absolutePath)
            if (existing != null) {
                scannedPaths.add(subDir.absolutePath)
                val freshTags = collectBookTags(subDir.absolutePath)
                if (existing.tags != freshTags) {
                    bookDao.update(existing.copy(tags = freshTags))
                }
                continue
            }

            val imageFiles = subDir.listFiles { f ->
                f.isFile && f.extension.lowercase() in imageExtensions
            }?.sortedBy { it.name } ?: emptyList()

            val childDirs = subDir.listFiles { f -> f.isDirectory }?.toList() ?: emptyList()

            if (imageFiles.isNotEmpty() && childDirs.isEmpty()) {
                val name = subDir.name
                val authorPattern = Regex("""^[\[［](.+?)[\]］]\s*(.*)""")
                val match = authorPattern.find(name)
                val ownAuthor = match?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                val title = match?.groupValues?.getOrNull(2)?.takeIf { it.isNotBlank() } ?: name
                val author = ownAuthor ?: inheritedAuthor

                val entity = BookEntity(
                    title = title,
                    author = author,
                    tags = collectBookTags(subDir.absolutePath),
                    coverPath = imageFiles.first().absolutePath,
                    folderPath = subDir.absolutePath,
                    pageCount = imageFiles.size,
                    lastReadTime = 0,
                    createdAt = System.currentTimeMillis()
                )
                bookDao.insert(entity)
                newBooks.add(entity.toDomain())
                scannedPaths.add(subDir.absolutePath)
            } else {
                val authorPattern = Regex("""^[\[［](.+?)[\]］]\s*(.*)""")
                val match = authorPattern.find(subDir.name)
                val ownAuthor = match?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }
                val nextAuthor = ownAuthor ?: inheritedAuthor
                scanDirectoryRecursive(subDir, imageExtensions, newBooks, scannedPaths, nextAuthor)
            }
        }
    }

    override suspend fun incrementViewCount(id: Long) {
        bookDao.incrementViewCount(id, System.currentTimeMillis())
    }

    override suspend fun addDuration(id: Long, duration: Long) {
        bookDao.addDuration(id, duration)
    }

    override fun searchBooks(query: String, mode: SearchMode): Flow<List<Book>> {
        return when (mode) {
            SearchMode.TITLE -> bookDao.searchByTitle(query)
            SearchMode.AUTHOR -> bookDao.searchByAuthor(query)
            SearchMode.TAGS -> bookDao.searchByTags(query)
        }.map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getDailyRecommendations(count: Int, seed: Long): List<Book> {
        val ids = bookDao.getAllIds()
        if (ids.isEmpty()) return emptyList()
        val shuffled = ids.shuffled(java.util.Random(seed))
        val selected = shuffled.take(count)
        return bookDao.getByIds(selected).map { it.toDomain() }
    }

    override fun getImagesInBook(book: Book): List<String> {
        val folder = File(resolvePath(book.folderPath))
        if (!folder.exists()) return emptyList()
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
        return folder.listFiles { f ->
            f.isFile && f.extension.lowercase() in imageExtensions
        }?.sortedBy { it.name }?.map { it.absolutePath } ?: emptyList()
    }

    override fun getRecentlyReadBooks(limit: Int): Flow<List<Book>> {
        return bookDao.getRecentlyRead(limit).map { entities -> entities.map { it.toDomain() } }
    }

    private fun resolvePath(path: String): String {
        if (!path.startsWith("content://")) return path
        return try {
            val uri = android.net.Uri.parse(path)
            val segment = uri.lastPathSegment ?: return path
            val decoded = java.net.URLDecoder.decode(segment, "UTF-8")
            val colonIndex = decoded.indexOf(':')
            if (colonIndex > 0) {
                val root = decoded.substring(0, colonIndex)
                val subPath = decoded.substring(colonIndex + 1)
                if (root == "primary") "/storage/emulated/0/$subPath"
                else "/storage/$root/$subPath"
            } else decoded
        } catch (_: Exception) { path }
    }

    private suspend fun collectBookTags(folderPath: String): String {
        val tagNames = mutableSetOf<String>()
        val tagIds = fileTagDao.getTagIdsByFilePath(folderPath)
        for (tagId in tagIds) {
            val tag = tagDao.getById(tagId)
            tag?.let { tagNames.add(it.name) }
        }
        return tagNames.joinToString("/")
    }

    override suspend fun updateBookDescription(id: Long, description: String) {
        bookDao.updateDescription(id, description)
    }

    override suspend fun updateBookScore(id: Long, score: Float) {
        bookDao.updateScore(id, score)
    }

    override suspend fun updateBookTags(id: Long, tags: String) {
        val book = bookDao.getById(id) ?: return
        val folderPath = book.folderPath
        val newTagNames = tags.split("/").filter { it.isNotBlank() }.toSet()
        val existingTagIds = fileTagDao.getTagIdsByFilePath(folderPath).toSet()
        val allTags = tagDao.getAll().firstOrNull() ?: emptyList()

        val newTagIds = allTags.filter { it.name in newTagNames }.map { it.id }.toSet()
        val tagsToRemove = existingTagIds - newTagIds
        val tagsToAdd = newTagIds - existingTagIds

        for (tagId in tagsToRemove) {
            fileTagDao.deleteCrossRefByFileAndTag(folderPath, tagId)
        }
        for (tagId in tagsToAdd) {
            fileTagDao.insertCrossRef(FileTagCrossRefEntity(filePath = folderPath, tagId = tagId))
        }

        bookDao.updateTags(id, newTagNames.joinToString("/"))
    }

    override fun getAllBookTags(): Flow<List<Tag>> {
        return tagDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun createBookTag(name: String, color: Int): Long {
        return tagDao.insert(TagEntity(name = name, color = color))
    }
}
