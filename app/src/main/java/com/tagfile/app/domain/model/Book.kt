package com.tagfile.app.domain.model

data class Book(
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val tags: String = "",
    val coverPath: String,
    val folderPath: String,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val viewCount: Int = 0,
    val totalDuration: Long = 0,
    val description: String = "",
    val score: Float = 0f,
    val lastReadTime: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
