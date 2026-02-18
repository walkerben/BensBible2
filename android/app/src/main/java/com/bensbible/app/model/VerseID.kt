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

        fun displayRange(verseIDs: Set<VerseID>): String {
            if (verseIDs.isEmpty()) return ""
            val sorted = verseIDs.sorted()
            val first = sorted.first()
            val book = first.book
            val chapter = first.chapter

            val verses = sorted.map { it.verse }
            val ranges = mutableListOf<String>()
            var rangeStart = verses[0]
            var rangeEnd = verses[0]

            for (i in 1 until verses.size) {
                if (verses[i] == rangeEnd + 1) {
                    rangeEnd = verses[i]
                } else {
                    ranges.add(if (rangeStart == rangeEnd) "$rangeStart" else "$rangeStart-$rangeEnd")
                    rangeStart = verses[i]
                    rangeEnd = verses[i]
                }
            }
            ranges.add(if (rangeStart == rangeEnd) "$rangeStart" else "$rangeStart-$rangeEnd")

            return "$book $chapter:${ranges.joinToString(", ")}"
        }
    }
}
