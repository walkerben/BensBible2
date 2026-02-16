package com.bensbible.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VerseAnnotationDao {

    @Query("SELECT * FROM verse_annotations WHERE bookName = :book AND chapterNumber = :chapter")
    suspend fun fetchByBookAndChapter(book: String, chapter: Int): List<VerseAnnotationEntity>

    @Query("SELECT * FROM verse_annotations WHERE verseKey = :key LIMIT 1")
    suspend fun fetchByKey(key: String): VerseAnnotationEntity?

    @Query("SELECT * FROM verse_annotations WHERE isBookmarked = 1 ORDER BY bookName, chapterNumber, verseNumber")
    suspend fun fetchAllBookmarks(): List<VerseAnnotationEntity>

    @Query("SELECT * FROM verse_annotations WHERE noteText IS NOT NULL AND noteText != '' ORDER BY bookName, chapterNumber, verseNumber")
    suspend fun fetchAllNotes(): List<VerseAnnotationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VerseAnnotationEntity)

    @Delete
    suspend fun delete(entity: VerseAnnotationEntity)
}
