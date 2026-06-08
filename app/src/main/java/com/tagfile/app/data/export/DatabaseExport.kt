package com.tagfile.app.data.export

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseExport(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val books: List<BookJson>,
    val tags: List<TagJson>,
    val fileIndex: List<FileIndexJson>,
    val fileTagCrossRefs: List<FileTagCrossRefJson>,
    val filterPresets: List<FilterPresetJson>
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
    val viewCount: Int = 0,
    val totalDuration: Long = 0,
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
    val sortOrder: Int = 0,
    val createdAt: Long
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