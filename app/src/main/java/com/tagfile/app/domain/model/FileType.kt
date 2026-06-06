package com.tagfile.app.domain.model

@Suppress("SpellCheckingInspection")
enum class FileType(val label: String, val extensions: List<String>) {
    IMAGE("图片", listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic")),
    VIDEO("视频", listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp", "webm")),
    AUDIO("音频", listOf("mp3", "wav", "aac", "flac", "ogg", "wma", "m4a")),
    DOCUMENT("文档", listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "md")),
    ARCHIVE("压缩包", listOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz")),
    APK("安装包", listOf("apk", "xapk", "apkm")),
    OTHER("其他", emptyList());

    companion object {
        fun fromExtension(extension: String): FileType {
            val lowerExt = extension.lowercase()
            return entries.firstOrNull { it != OTHER && lowerExt in it.extensions } ?: OTHER
        }
    }
}
