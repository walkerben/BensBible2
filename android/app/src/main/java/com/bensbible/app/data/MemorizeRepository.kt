package com.bensbible.app.data

import com.bensbible.app.util.Sm2Scheduler
import kotlinx.coroutines.flow.Flow

class MemorizeRepository(private val dao: MemorizeDao) {

    fun getAllVerses(): Flow<List<MemorizedVerseEntity>> = dao.getAllVerses()

    suspend fun addVerse(
        bookName: String,
        chapterNumber: Int,
        verseNumber: Int,
        verseText: String
    ) {
        dao.insertVerse(
            MemorizedVerseEntity(
                bookName = bookName,
                chapterNumber = chapterNumber,
                verseNumber = verseNumber,
                verseText = verseText
            )
        )
    }

    suspend fun deleteVerse(verse: MemorizedVerseEntity) = dao.deleteVerse(verse)

    suspend fun applyReview(
        verse: MemorizedVerseEntity,
        quality: Int,
        exerciseType: String
    ) {
        val result = Sm2Scheduler.process(
            quality = quality,
            repetitions = verse.repetitions,
            easeFactor = verse.easeFactor,
            intervalDays = verse.intervalDays
        )
        dao.updateVerse(
            verse.copy(
                repetitions = result.repetitions,
                easeFactor = result.easeFactor,
                intervalDays = result.intervalDays,
                nextReviewDate = result.nextReviewDate,
                lastReviewedAt = System.currentTimeMillis(),
                totalReviews = verse.totalReviews + 1
            )
        )
        dao.insertLog(
            MemoryReviewLogEntity(
                verseKey = verse.verseKey,
                quality = quality,
                exerciseType = exerciseType
            )
        )
    }

    suspend fun getCount(): Int = dao.getCount()

    suspend fun seedDefaultVersesIfNeeded() {
        if (dao.getCount() > 0) return
        val defaults = listOf(
            Triple("John", 3 to 16, "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."),
            Triple("Jeremiah", 29 to 11, "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end."),
            Triple("Philippians", 4 to 13, "I can do all things through Christ which strengtheneth me."),
            Triple("Romans", 8 to 28, "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."),
            Triple("Proverbs", 3 to 5, "Trust in the LORD with all thine heart; and lean not unto thine own understanding."),
            Triple("Isaiah", 40 to 31, "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint."),
            Triple("Joshua", 1 to 9, "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.")
        )
        defaults.forEach { (book, chapterVerse, text) ->
            dao.insertVerse(
                MemorizedVerseEntity(
                    bookName = book,
                    chapterNumber = chapterVerse.first,
                    verseNumber = chapterVerse.second,
                    verseText = text,
                    nextReviewDate = System.currentTimeMillis()
                )
            )
        }
    }
}
