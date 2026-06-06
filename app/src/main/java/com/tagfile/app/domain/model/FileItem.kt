package com.tagfile.app.domain.model

data class FileItem(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val mimeType: String? = null,
    val tags: List<Tag> = emptyList()
)
