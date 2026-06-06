package com.tagfile.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_presets")
data class FilterPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val strength: Float,
    val sharpness: Float,
    val denoise: Float,
    @ColumnInfo(name = "line_darkening")
    val lineDarkening: Float,
    val contrast: Float,
    val saturation: Float,
    @ColumnInfo(name = "upscale_factor")
    val upscaleFactor: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
