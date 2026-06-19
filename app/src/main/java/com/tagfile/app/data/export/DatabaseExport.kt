package com.tagfile.app.data.export

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseExport(
    val version: Int = 2,
    val exportedAt: Long = System.currentTimeMillis(),
    val books: List<BookJson>,
    val tags: List<TagJson>,
    val fileIndex: List<FileIndexJson>,
    val fileTagCrossRefs: List<FileTagCrossRefJson>,
    val filterPresets: List<FilterPresetJson>,
    val trash: List<TrashJson> = emptyList()
)

@Serializable
data class BookJson(
    val id: Long,
    val title: String,
    val author: String? = null,
    val tags: String,
    val coverPath: String,
    val folderPath: String,
    val pageCount: Int,
    val currentPage: Int = 0,
    val viewCount: Int = 0,
    val readDuration: Long = 0,
    val description: String = "",
    val score: Float = 0f,
    val lastReadTime: Long,
    val createdAt: Long
)

@Serializable
data class TagJson(
    val id: Long,
    val name: String,
    val color: Int,
    val icon: String? = null,
    val groupName: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long
)

@Serializable
data class TrashJson(
    val id: Long,
    val originalPath: String,
    val trashPath: String,
    val fileName: String,
    val isDirectory: Boolean = false,
    val deletedAt: Long,
    val fileSize: Long = 0
)

@Serializable
data class FileIndexJson(
    val path: String,
    val name: String,
    val nameLower: String,
    val isDirectory: Boolean,
    val extension: String,
    val size: Long,
    val lastModified: Long
)

@Serializable
data class FileTagCrossRefJson(
    val filePath: String,
    val tagId: Long,
    val isInherited: Boolean = false
)

@Serializable
data class FilterPresetJson(
    val id: Long,
    val name: String,
    val strength: Float,
    val sharpness: Float,
    val denoise: Float,
    val lineDarkening: Float,
    val contrast: Float,
    val saturation: Float,
    val upscaleFactor: Int,
    val createdAt: Long
)