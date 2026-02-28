package com.bensbible.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VerseAnnotationEntity::class, PresentationEntity::class, PresentationSlideEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun verseAnnotationDao(): VerseAnnotationDao
    abstract fun presentationDao(): PresentationDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS presentations (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS presentation_slides (
                        id TEXT NOT NULL PRIMARY KEY,
                        presentationId TEXT NOT NULL,
                        bookName TEXT NOT NULL,
                        chapterNumber INTEGER NOT NULL,
                        verseNumber INTEGER NOT NULL,
                        verseText TEXT NOT NULL,
                        `order` INTEGER NOT NULL,
                        FOREIGN KEY (presentationId) REFERENCES presentations(id) ON DELETE CASCADE
                    )"""
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_presentation_slides_presentationId ON presentation_slides(presentationId)"
                )
            }
        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bensbible.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}
