package com.tagfile.app.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.request.CachePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .diskCache(
                DiskCache.Builder()
                    .directory(java.io.File(context.cacheDir, "image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build()
            )
            .memoryCachePolicy(CachePolicy.ENABLED)
            .allowHardware(false)
            .crossfade(true)
            .build()
    }
}
