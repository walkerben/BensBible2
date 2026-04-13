package com.bensbible.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        VerseAnnotationEntity::class,
        PresentationEntity::class,
        PresentationSlideEntity::class,
        MemorizedVerseEntity::class,
        MemoryReviewLogEntity::class,
        ReadingPlanProgressEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun verseAnnotationDao(): VerseAnnotationDao
    abstract fun presentationDao(): PresentationDao
    abstract fun memorizeDao(): MemorizeDao
    abstract fun readingPlanDao(): ReadingPlanDao

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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS memorized_verses (
                        id TEXT NOT NULL PRIMARY KEY,
                        bookName TEXT NOT NULL,
                        chapterNumber INTEGER NOT NULL,
                        verseNumber INTEGER NOT NULL,
                        verseText TEXT NOT NULL,
                        repetitions INTEGER NOT NULL DEFAULT 0,
                        easeFactor REAL NOT NULL DEFAULT 2.5,
                        intervalDays INTEGER NOT NULL DEFAULT 1,
                        nextReviewDate INTEGER NOT NULL,
                        addedAt INTEGER NOT NULL,
                        lastReviewedAt INTEGER,
                        totalReviews INTEGER NOT NULL DEFAULT 0
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS memory_review_logs (
                        id TEXT NOT NULL PRIMARY KEY,
                        verseKey TEXT NOT NULL,
                        reviewedAt INTEGER NOT NULL,
                        quality INTEGER NOT NULL,
                        exerciseType TEXT NOT NULL
                    )"""
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reading_plan_progress (
                        id TEXT NOT NULL PRIMARY KEY,
                        planId TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        completedAt INTEGER,
                        completedDaysData TEXT NOT NULL DEFAULT ''
                    )
                """)
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS " +
                    "index_memorized_verses_book_chapter_verse " +
                    "ON memorized_verses(bookName, chapterNumber, verseNumber)"
                )
            }
        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "bensbible.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys = ON")
                    }
                })
                .build()
        }
    }
}
