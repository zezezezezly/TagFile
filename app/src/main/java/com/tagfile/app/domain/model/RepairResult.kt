package com.tagfile.app.domain.model

data class RepairResult(
    val bookId: Long,
    val bookTitle: String,
    val fixes: List<String>,
    val hasIssues: Boolean
)
