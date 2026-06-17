package com.tagfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trash")
data class TrashEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "original_path")
    val originalPath: String,
    @ColumnInfo(name = "trash_path")
    val trashPath: String,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "is_directory")
    val isDirectory: Boolean = false,
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0
)