package com.tagfile.app.domain.model

data class FileTagCrossRef(
    val filePath: String,
    val tagId: Long,
    val isInherited: Boolean = false
)
