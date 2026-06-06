package com.tagfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "file_index", primaryKeys = ["path"])
data class FileIndexEntity(
    @ColumnInfo(name = "path")
    val path: String,
    val name: String,
    @ColumnInfo(name = "name_lower")
    val nameLower: String,
    @ColumnInfo(name = "is_directory")
    val isDirectory: Boolean,
    val extension: String,
    val size: Long,
    @ColumnInfo(name = "last_modified")
    val lastModified: Long
)
