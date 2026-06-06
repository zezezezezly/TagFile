package com.tagfile.app.domain.model

enum class TagMode { AND, OR }

data class SearchFilter(
    val keyword: String = "",
    val tagIds: List<Long> = emptyList(),
    val tagMode: TagMode = TagMode.AND,
    val fileTypes: Set<FileType> = emptySet(),
    val searchDirectories: Boolean = false,
    val minSize: Long? = null,
    val maxSize: Long? = null,
    val dateFrom: Long? = null,
    val dateTo: Long? = null
)
