package com.tagfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String? = null,
    val tags: String,
    @ColumnInfo(name = "cover_path")
    val coverPath: String,
    @ColumnInfo(name = "folder_path")
    val folderPath: String,
    @ColumnInfo(name = "page_count")
    val pageCount: Int,
    @ColumnInfo(name = "current_page")
    val currentPage: Int = 0,
    @ColumnInfo(name = "view_count")
    val viewCount: Int = 0,
    @ColumnInfo(name = "read_duration")
    val readDuration: Long = 0,
    @ColumnInfo(name = "description", defaultValue = "")
    val description: String = "",
    @ColumnInfo(name = "score", defaultValue = "0.0")
    val score: Float = 0f,
    @ColumnInfo(name = "last_read_time")
    val lastReadTime: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
