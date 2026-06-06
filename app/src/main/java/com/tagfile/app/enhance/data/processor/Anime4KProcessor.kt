package com.tagfile.app.enhance.data.processor

import android.graphics.Bitmap
import com.tagfile.app.enhance.data.processor.gl.GpuProcessor
import com.tagfile.app.enhance.domain.model.EnhanceParams
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Anime4KProcessor @Inject constructor() {

    private val gpuDispatcher = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "Anime4K-GPU").apply {
            isDaemon = true
        }
    }.asCoroutineDispatcher()

    private var gpu: GpuProcessor? = null
    private val mutex = Mutex()

    suspend fun process(source: Bitmap, params: EnhanceParams): Bitmap =
        mutex.withLock {
            withContext(gpuDispatcher) {
                val processor = gpu ?: GpuProcessor().also { gpu = it }
                processor.process(source, params)
            }
        }

    fun release() {
        runBlocking {
            mutex.withLock {
                gpu?.release()
                gpu = null
            }
        }
    }
}
