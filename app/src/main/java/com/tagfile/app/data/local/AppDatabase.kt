package com.tagfile.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.dao.FilterPresetDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.entity.BookEntity
import com.tagfile.app.data.local.entity.FileIndexEntity
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity
import com.tagfile.app.data.local.entity.FilterPresetEntity
import com.tagfile.app.data.local.entity.TagEntity

@Database(
    entities = [
        TagEntity::class,
        FileTagCrossRefEntity::class,
        FileIndexEntity::class,
        FilterPresetEntity::class,
        BookEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun fileTagDao(): FileTagDao
    abstract fun fileIndexDao(): FileIndexDao
    abstract fun filterPresetDao(): FilterPresetDao
    abstract fun bookDao(): BookDao
}
