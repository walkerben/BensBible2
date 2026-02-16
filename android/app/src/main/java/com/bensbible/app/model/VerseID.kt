package com.bensbible.app.model

data class VerseID(
    val book: String,
    val chapter: Int,
    val verse: Int
) : Comparable<VerseID> {

    val key: String get() = "$book:$chapter:$verse"
    val displayReference: String get() = "$book $chapter:$verse"

    override fun toString(): String = key

    override fun compareTo(other: VerseID): Int {
        val bookCmp = book.compareTo(other.book)
        if (bookCmp != 0) return bookCmp
        val chapterCmp = chapter.compareTo(other.chapter)
        if (chapterCmp != 0) return chapterCmp
        return verse.compareTo(other.verse)
    }

    companion object {
        fun fromKey(key: String): VerseID? {
            val parts = key.split(":")
            if (parts.size != 3) return null
            val chapter = parts[1].toIntOrNull() ?: return null
            val verse = parts[2].toIntOrNull() ?: return null
            return VerseID(book = parts[0], chapter = chapter, verse = verse)
        }
    }
}
