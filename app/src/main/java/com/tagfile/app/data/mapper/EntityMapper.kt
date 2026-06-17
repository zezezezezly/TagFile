package com.tagfile.app.data.mapper

import com.tagfile.app.data.local.entity.BookEntity
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity
import com.tagfile.app.data.local.entity.TagEntity
import com.tagfile.app.domain.model.Book
import com.tagfile.app.domain.model.FileTagCrossRef
import com.tagfile.app.domain.model.Tag

object EntityMapper {

    fun TagEntity.toDomain(): Tag {
        return Tag(
            id = id,
            name = name,
            color = color,
            icon = icon,
            groupName = groupName,
            sortOrder = sortOrder,
            createdAt = createdAt
        )
    }

    fun Tag.toEntity(): TagEntity {
        return TagEntity(
            id = id,
            name = name,
            color = color,
            icon = icon,
            groupName = groupName,
            sortOrder = sortOrder,
            createdAt = createdAt
        )
    }

    @Suppress("unused")
    fun FileTagCrossRefEntity.toDomain(): FileTagCrossRef {
        return FileTagCrossRef(
            filePath = filePath,
            tagId = tagId,
            isInherited = isInherited
        )
    }

    fun BookEntity.toDomain(): Book {
        return Book(
            id = id,
            title = title,
            author = author,
            tags = tags,
            coverPath = coverPath,
            folderPath = folderPath,
            pageCount = pageCount,
            currentPage = currentPage,
            viewCount = viewCount,
            totalDuration = totalDuration,
            description = description,
            score = score,
            lastReadTime = lastReadTime,
            createdAt = createdAt
        )
    }

    fun Book.toEntity(): BookEntity {
        return BookEntity(
            id = id,
            title = title,
            author = author,
            tags = tags,
            coverPath = coverPath,
            folderPath = folderPath,
            pageCount = pageCount,
            currentPage = currentPage,
            viewCount = viewCount,
            totalDuration = totalDuration,
            description = description,
            score = score,
            lastReadTime = lastReadTime,
            createdAt = createdAt
        )
    }
}
