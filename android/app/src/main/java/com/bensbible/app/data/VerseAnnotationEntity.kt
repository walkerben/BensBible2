package com.bensbible.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bensbible.app.model.HighlightColor
import com.bensbible.app.model.VerseID

@Entity(tableName = "verse_annotations")
data class VerseAnnotationEntity(
    @PrimaryKey
    val verseKey: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val highlightColorRaw: String? = null,
    val noteText: String? = null,
    val isBookmarked: Boolean = false
) {
    val highlightColor: HighlightColor?
        get() = HighlightColor.fromRawValue(highlightColorRaw)

    val isEmpty: Boolean
        get() = highlightColorRaw == null && noteText.isNullOrEmpty() && !isBookmarked

    val verseID: VerseID
        get() = VerseID(book = bookName, chapter = chapterNumber, verse = verseNumber)

    companion object {
        fun create(verseID: VerseID): VerseAnnotationEntity {
            return VerseAnnotationEntity(
                verseKey = verseID.key,
                bookName = verseID.book,
                chapterNumber = verseID.chapter,
                verseNumber = verseID.verse
            )
        }
    }
}
