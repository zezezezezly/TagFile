package com.tagfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "file_tag_cross_ref",
    primaryKeys = ["file_path", "tag_id"]
)
data class FileTagCrossRefEntity(
    @ColumnInfo(name = "file_path")
    val filePath: String,
    @ColumnInfo(name = "tag_id")
    val tagId: Long,
    @ColumnInfo(name = "is_inherited")
    val isInherited: Boolean = false
)
