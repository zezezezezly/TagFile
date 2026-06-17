package com.tagfile.app.domain.model

data class Tag(
    val id: Long = 0,
    val name: String,
    val color: Int,
    val icon: String? = null,
    val groupName: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
