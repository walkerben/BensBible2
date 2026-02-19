package com.bensbible.app.model

enum class BookGroup(val displayName: String, val indexRange: IntRange) {
    ALL("All", 0..65),
    OLD_TESTAMENT("Old Testament", 0..38),
    NEW_TESTAMENT("New Testament", 39..65),
    LAW("Law", 0..4),
    HISTORY("History", 5..16),
    POETRY("Poetry", 17..21),
    PROPHETS("Prophets", 22..38),
    GOSPELS("Gospels", 39..42),
    ACTS("Acts", 43..43),
    EPISTLES("Epistles", 44..64),
    REVELATION("Revelation", 65..65);

    fun filterBooks(allBooks: List<String>): List<String> {
        val start = indexRange.first.coerceAtLeast(0)
        val end = indexRange.last.coerceAtMost(allBooks.size - 1)
        if (start > end || start >= allBooks.size) return emptyList()
        return allBooks.subList(start, end + 1)
    }

    companion object {
        val pickerSections = listOf(LAW, HISTORY, POETRY, PROPHETS, GOSPELS, ACTS, EPISTLES, REVELATION)
        val searchFilters = entries.toList()
    }
}
