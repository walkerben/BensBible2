package com.bensbible.app.model

data class BibleLocation(
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int? = null
) {
    val displayTitle: String get() = "$bookName $chapterNumber"

    companion object {
        val genesis1 = BibleLocation(bookName = "Genesis", chapterNumber = 1)
    }
}
