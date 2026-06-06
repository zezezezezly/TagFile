package com.tagfile.app.data.filesystem

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import coil.ImageLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailProvider @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val imageLoader = ImageLoader.Builder(context)
        .crossfade(true)
        .build()

    @Suppress("unused")
    suspend fun loadThumbnail(filePath: String): Any? = withContext(Dispatchers.IO) {
        val file = File(filePath)
        if (!file.exists()) return@withContext null

        val ext = file.extension.lowercase()

        when (ext) {
            in imageExtensions -> {
                try {
                    BitmapFactory.decodeFile(filePath)
                } catch (_: Exception) { null }
            }
            in videoExtensions -> {
                try {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(filePath)
                        retriever.frameAtTime
                    } finally {
                        retriever.release()
                    }
                } catch (_: Exception) { null }
            }
            else -> null
        }
    }

    @Suppress("unused")
    fun getImageLoader(): ImageLoader = imageLoader

    companion object {
        @Suppress("SpellCheckingInspection")
        val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif")
        val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "3gp", "webm")
    }
}
