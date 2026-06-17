package com.tagfile.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tagfile.app.data.local.dao.BookDao
import com.tagfile.app.data.local.dao.FileIndexDao
import com.tagfile.app.data.local.dao.FileTagDao
import com.tagfile.app.data.local.dao.FilterPresetDao
import com.tagfile.app.data.local.dao.TagDao
import com.tagfile.app.data.local.dao.TrashDao
import com.tagfile.app.data.local.entity.BookEntity
import com.tagfile.app.data.local.entity.FileIndexEntity
import com.tagfile.app.data.local.entity.FileTagCrossRefEntity
import com.tagfile.app.data.local.entity.FilterPresetEntity
import com.tagfile.app.data.local.entity.TagEntity
import com.tagfile.app.data.local.entity.TrashEntity

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tags ADD COLUMN group_name TEXT")
        db.execSQL("ALTER TABLE books ADD COLUMN current_page INTEGER NOT NULL DEFAULT 0")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS trash (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                original_path TEXT NOT NULL,
                trash_path TEXT NOT NULL,
                file_name TEXT NOT NULL,
                is_directory INTEGER NOT NULL DEFAULT 0,
                deleted_at INTEGER NOT NULL,
                file_size INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

@Database(
    entities = [
        TagEntity::class,
        FileTagCrossRefEntity::class,
        FileIndexEntity::class,
        FilterPresetEntity::class,
        BookEntity::class,
        TrashEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun fileTagDao(): FileTagDao
    abstract fun fileIndexDao(): FileIndexDao
    abstract fun filterPresetDao(): FilterPresetDao
    abstract fun bookDao(): BookDao
    abstract fun trashDao(): TrashDao
}
