package com.tagfile.app.di

import android.content.Context
import androidx.room.Room
import com.tagfile.app.data.local.AppDatabase
import com.tagfile.app.data.local.MIGRATION_6_7
import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.dao.FilterPresetDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.dao.TrashDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tagfile.db"
        ).addMigrations(MIGRATION_6_7)
            .build()
    }

    @Provides
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideFileTagDao(database: AppDatabase): FileTagDao {
        return database.fileTagDao()
    }

    @Provides
    fun provideFileIndexDao(database: AppDatabase): FileIndexDao {
        return database.fileIndexDao()
    }

    @Provides
    fun provideFilterPresetDao(database: AppDatabase): FilterPresetDao {
        return database.filterPresetDao()
    }

    @Provides
    @Singleton
    fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()

    @Provides
    @Singleton
    fun provideTrashDao(db: AppDatabase): TrashDao = db.trashDao()
}
