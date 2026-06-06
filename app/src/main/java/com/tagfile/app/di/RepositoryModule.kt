package com.tagfile.app.di

import com.tagfile.app.data.repository.FileRepositoryImpl
import com.tagfile.app.data.repository.SearchRepositoryImpl
import com.tagfile.app.data.repository.ShelfRepositoryImpl
import com.tagfile.app.data.repository.TagRepositoryImpl
import com.tagfile.app.domain.repository.FileRepository
import com.tagfile.app.domain.repository.SearchRepository
import com.tagfile.app.domain.repository.ShelfRepository
import com.tagfile.app.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFileRepository(impl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds
    @Singleton
    abstract fun bindSearchRepository(impl: SearchRepositoryImpl): SearchRepository

    @Binds
    @Singleton
    abstract fun bindShelfRepository(impl: ShelfRepositoryImpl): ShelfRepository
}
