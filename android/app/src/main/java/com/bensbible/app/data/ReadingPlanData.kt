package com.bensbible.app.data

data class ReadingEntry(val bookName: String, val chapter: Int)

data class ReadingPlanDay(
    val dayNumber: Int,
    val readings: List<ReadingEntry>
) {
    val referenceText: String get() {
        if (readings.isEmpty()) return ""
        val parts = mutableListOf<String>()
        var i = 0
        while (i < readings.size) {
            val book = readings[i].bookName
            val start = readings[i].chapter
            var end = start
            while (i + 1 < readings.size && readings[i + 1].bookName == book && readings[i + 1].chapter == end + 1) {
                i++; end = readings[i].chapter
            }
            parts.add(if (start == end) "$book $start" else "$book $start\u2013$end")
            i++
        }
        return parts.joinToString(", ")
    }
}

data class ReadingPlan(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val totalDays: Int,
    val days: List<ReadingPlanDay>
)

private val bibleBooks = listOf(
    "Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36, "Deuteronomy" to 34,
    "Joshua" to 24, "Judges" to 21, "Ruth" to 4, "1 Samuel" to 31, "2 Samuel" to 24,
    "1 Kings" to 22, "2 Kings" to 25, "1 Chronicles" to 29, "2 Chronicles" to 36,
    "Ezra" to 10, "Nehemiah" to 13, "Esther" to 10,
    "Job" to 42, "Psalms" to 150, "Proverbs" to 31, "Ecclesiastes" to 12, "Song of Solomon" to 8,
    "Isaiah" to 66, "Jeremiah" to 52, "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12,
    "Hosea" to 14, "Joel" to 3, "Amos" to 9, "Obadiah" to 1, "Jonah" to 4, "Micah" to 7,
    "Nahum" to 3, "Habakkuk" to 3, "Zephaniah" to 3, "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4,
    "Matthew" to 28, "Mark" to 16, "Luke" to 24, "John" to 21, "Acts" to 28,
    "Romans" to 16, "1 Corinthians" to 16, "2 Corinthians" to 13, "Galatians" to 6,
    "Ephesians" to 6, "Philippians" to 4, "Colossians" to 4, "1 Thessalonians" to 5,
    "2 Thessalonians" to 3, "1 Timothy" to 6, "2 Timothy" to 4, "Titus" to 3, "Philemon" to 1,
    "Hebrews" to 13, "James" to 5, "1 Peter" to 5, "2 Peter" to 3,
    "1 John" to 5, "2 John" to 1, "3 John" to 1, "Jude" to 1, "Revelation" to 22
)

private fun distribute(entries: List<ReadingEntry>, days: Int): List<ReadingPlanDay> {
    val total = entries.size
    val result = mutableListOf<ReadingPlanDay>()
    for (day in 1..days) {
        val startIdx = (day - 1) * total / days
        val endIdx = day * total / days
        if (endIdx > startIdx) {
            result.add(ReadingPlanDay(dayNumber = day, readings = entries.subList(startIdx, endIdx)))
        }
    }
    return result
}

private fun chronologicalSequence(): List<ReadingEntry> {
    val segments = listOf(
        Triple("Genesis", 1, 11), Triple("Job", 1, 42), Triple("Genesis", 12, 50),
        Triple("Exodus", 1, 40), Triple("Leviticus", 1, 27), Triple("Numbers", 1, 36), Triple("Deuteronomy", 1, 34),
        Triple("Joshua", 1, 24), Triple("Judges", 1, 21), Triple("Ruth", 1, 4),
        Triple("1 Samuel", 1, 31), Triple("2 Samuel", 1, 24), Triple("Psalms", 1, 72),
        Triple("1 Kings", 1, 11), Triple("Proverbs", 1, 31), Triple("Ecclesiastes", 1, 12), Triple("Song of Solomon", 1, 8),
        Triple("1 Kings", 12, 22), Triple("2 Kings", 1, 25), Triple("1 Chronicles", 1, 29), Triple("2 Chronicles", 1, 36),
        Triple("Isaiah", 1, 66), Triple("Jeremiah", 1, 52), Triple("Lamentations", 1, 5),
        Triple("Ezekiel", 1, 48), Triple("Daniel", 1, 12),
        Triple("Hosea", 1, 14), Triple("Joel", 1, 3), Triple("Amos", 1, 9), Triple("Obadiah", 1, 1),
        Triple("Jonah", 1, 4), Triple("Micah", 1, 7), Triple("Nahum", 1, 3), Triple("Habakkuk", 1, 3),
        Triple("Zephaniah", 1, 3), Triple("Haggai", 1, 2), Triple("Zechariah", 1, 14), Triple("Malachi", 1, 4),
        Triple("Ezra", 1, 10), Triple("Nehemiah", 1, 13), Triple("Esther", 1, 10), Triple("Psalms", 73, 150),
        Triple("Matthew", 1, 28), Triple("Mark", 1, 16), Triple("Luke", 1, 24), Triple("John", 1, 21), Triple("Acts", 1, 28),
        Triple("Romans", 1, 16), Triple("1 Corinthians", 1, 16), Triple("2 Corinthians", 1, 13), Triple("Galatians", 1, 6),
        Triple("Ephesians", 1, 6), Triple("Philippians", 1, 4), Triple("Colossians", 1, 4),
        Triple("1 Thessalonians", 1, 5), Triple("2 Thessalonians", 1, 3), Triple("1 Timothy", 1, 6), Triple("2 Timothy", 1, 4),
        Triple("Titus", 1, 3), Triple("Philemon", 1, 1), Triple("Hebrews", 1, 13), Triple("James", 1, 5),
        Triple("1 Peter", 1, 5), Triple("2 Peter", 1, 3), Triple("1 John", 1, 5), Triple("2 John", 1, 1),
        Triple("3 John", 1, 1), Triple("Jude", 1, 1), Triple("Revelation", 1, 22)
    )
    return segments.flatMap { (book, start, end) -> (start..end).map { ReadingEntry(bookName = book, chapter = it) } }
}

private fun blendedSequence(): List<ReadingPlanDay> {
    val otBooks = bibleBooks.take(39)
    val ntBooks = bibleBooks.drop(39)
    val otEntries = otBooks.flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }
    val ntEntries = ntBooks.flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }
    val otDays = distribute(otEntries, 365).associateBy { it.dayNumber }
    val ntDays = distribute(ntEntries, 365).associateBy { it.dayNumber }
    return (1..365).map { day ->
        val ot = otDays[day]?.readings ?: emptyList()
        val nt = ntDays[day]?.readings ?: emptyList()
        ReadingPlanDay(dayNumber = day, readings = ot + nt)
    }
}

private fun buildPlans(): List<ReadingPlan> {
    val allEntries = bibleBooks.flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // NT books (last 27)
    val ntBooks = bibleBooks.drop(39)
    val ntEntries = ntBooks.flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Gospel books: Matthew(28), Mark(16), Luke(24), John(21)
    val gospelEntries = listOf("Matthew" to 28, "Mark" to 16, "Luke" to 24, "John" to 21)
        .flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Acts & Paul's letters
    val actsPaulEntries = listOf(
        "Acts" to 28, "Romans" to 16, "1 Corinthians" to 16, "2 Corinthians" to 13,
        "Galatians" to 6, "Ephesians" to 6, "Philippians" to 4, "Colossians" to 4,
        "1 Thessalonians" to 5, "2 Thessalonians" to 3, "1 Timothy" to 6, "2 Timothy" to 4,
        "Titus" to 3, "Philemon" to 1
    ).flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Letters & Revelation
    val lettersRevEntries = listOf(
        "Hebrews" to 13, "James" to 5, "1 Peter" to 5, "2 Peter" to 3,
        "1 John" to 5, "2 John" to 1, "3 John" to 1, "Jude" to 1, "Revelation" to 22
    ).flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Torah
    val torahEntries = listOf("Genesis" to 50, "Exodus" to 40, "Leviticus" to 27, "Numbers" to 36, "Deuteronomy" to 34)
        .flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Wisdom & Poetry
    val wisdomEntries = listOf("Job" to 42, "Proverbs" to 31, "Ecclesiastes" to 12, "Song of Solomon" to 8)
        .flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Major Prophets
    val majorProphetsEntries = listOf("Isaiah" to 66, "Jeremiah" to 52, "Lamentations" to 5, "Ezekiel" to 48, "Daniel" to 12)
        .flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Minor Prophets
    val minorProphetsEntries = listOf(
        "Hosea" to 14, "Joel" to 3, "Amos" to 9, "Obadiah" to 1, "Jonah" to 4, "Micah" to 7,
        "Nahum" to 3, "Habakkuk" to 3, "Zephaniah" to 3, "Haggai" to 2, "Zechariah" to 14, "Malachi" to 4
    ).flatMap { (book, count) -> (1..count).map { ReadingEntry(bookName = book, chapter = it) } }

    // Psalms
    val psalmsEntries = (1..150).map { ReadingEntry(bookName = "Psalms", chapter = it) }

    return listOf(
        ReadingPlan(
            id = "bible-sequential",
            title = "Bible in a Year \u2014 Sequential",
            description = "Read through the entire Bible from Genesis to Revelation in one year.",
            category = "Bible in a Year",
            totalDays = 365,
            days = distribute(allEntries, 365)
        ),
        ReadingPlan(
            id = "bible-chronological",
            title = "Bible in a Year \u2014 Chronological",
            description = "Read the Bible in the order events occurred historically.",
            category = "Bible in a Year",
            totalDays = 365,
            days = distribute(chronologicalSequence(), 365)
        ),
        ReadingPlan(
            id = "bible-blended",
            title = "Bible in a Year \u2014 Blended",
            description = "Each day includes both Old and New Testament readings.",
            category = "Bible in a Year",
            totalDays = 365,
            days = blendedSequence()
        ),
        ReadingPlan(
            id = "new-testament-90",
            title = "New Testament in 90 Days",
            description = "Read through the entire New Testament in just 90 days.",
            category = "New Testament",
            totalDays = 90,
            days = distribute(ntEntries, 90)
        ),
        ReadingPlan(
            id = "gospels",
            title = "The Gospels",
            description = "Read all four Gospels \u2014 Matthew, Mark, Luke, and John.",
            category = "New Testament",
            totalDays = 89,
            days = distribute(gospelEntries, 89)
        ),
        ReadingPlan(
            id = "acts-paul",
            title = "Acts & Paul's Letters",
            description = "Follow Paul's missionary journeys and letters to the early churches.",
            category = "New Testament",
            totalDays = 121,
            days = distribute(actsPaulEntries, 121)
        ),
        ReadingPlan(
            id = "letters-revelation",
            title = "Letters & Revelation",
            description = "Read Hebrews through Revelation, including the general epistles and Apocalypse.",
            category = "New Testament",
            totalDays = 56,
            days = distribute(lettersRevEntries, 56)
        ),
        ReadingPlan(
            id = "torah",
            title = "Torah \u2014 Five Books of Moses",
            description = "Explore the foundation of Scripture through the Pentateuch.",
            category = "Old Testament",
            totalDays = 94,
            days = distribute(torahEntries, 94)
        ),
        ReadingPlan(
            id = "wisdom-poetry",
            title = "Wisdom & Poetry",
            description = "Journey through Job, Proverbs, Ecclesiastes, and Song of Solomon.",
            category = "Old Testament",
            totalDays = 93,
            days = distribute(wisdomEntries, 93)
        ),
        ReadingPlan(
            id = "major-prophets",
            title = "The Major Prophets",
            description = "Read Isaiah, Jeremiah, Lamentations, Ezekiel, and Daniel.",
            category = "Old Testament",
            totalDays = 92,
            days = distribute(majorProphetsEntries, 92)
        ),
        ReadingPlan(
            id = "minor-prophets",
            title = "The Minor Prophets",
            description = "Read all twelve minor prophets, from Hosea to Malachi.",
            category = "Old Testament",
            totalDays = 34,
            days = distribute(minorProphetsEntries, 34)
        ),
        ReadingPlan(
            id = "psalms-30",
            title = "Psalms in 30 Days",
            description = "Read all 150 Psalms in 30 days, five psalms each day.",
            category = "Old Testament",
            totalDays = 30,
            days = distribute(psalmsEntries, 30)
        )
    )
}

val allReadingPlans: List<ReadingPlan> by lazy { buildPlans() }
