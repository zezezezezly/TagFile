package com.tagfile.app.di

import coil.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ImageViewerEntryPoint {
    @get:Named("viewer")
    val viewerImageLoader: ImageLoader
}
