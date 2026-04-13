package com.bensbible.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "memorized_verses",
    indices = [Index(value = ["bookName", "chapterNumber", "verseNumber"], unique = true)]
)
data class MemorizedVerseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseText: String,
    // SM-2 fields
    val repetitions: Int = 0,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 1,
    val nextReviewDate: Long = System.currentTimeMillis(),
    // Metadata
    val addedAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val totalReviews: Int = 0
) {
    val verseKey: String get() = "$bookName $chapterNumber:$verseNumber"
    val reference: String get() = "$bookName $chapterNumber:$verseNumber"
    val isDue: Boolean get() = nextReviewDate <= System.currentTimeMillis()
}
