import Foundation

// MARK: - Value Types

struct ReadingEntry {
    let bookName: String
    let chapter: Int
}

struct ReadingPlanDay {
    let dayNumber: Int
    let readings: [ReadingEntry]

    /// Compact reference, e.g. "Genesis 1–3" or "Genesis 50, Exodus 1"
    var referenceText: String {
        guard !readings.isEmpty else { return "" }
        // Group consecutive chapters of the same book
        var groups: [(book: String, chapters: [Int])] = []
        for entry in readings {
            if let last = groups.last, last.book == entry.bookName {
                groups[groups.count - 1].chapters.append(entry.chapter)
            } else {
                groups.append((book: entry.bookName, chapters: [entry.chapter]))
            }
        }
        return groups.map { group in
            let chapters = group.chapters
            if chapters.count == 1 {
                return "\(group.book) \(chapters[0])"
            } else {
                return "\(group.book) \(chapters.first!)–\(chapters.last!)"
            }
        }.joined(separator: ", ")
    }
}

struct ReadingPlan {
    let id: String
    let title: String
    let description: String
    let category: String
    let totalDays: Int
    let days: [ReadingPlanDay]
}

// MARK: - Bible Book Data

private let bibleBooks: [(String, Int)] = [
    ("Genesis", 50), ("Exodus", 40), ("Leviticus", 27), ("Numbers", 36), ("Deuteronomy", 34),
    ("Joshua", 24), ("Judges", 21), ("Ruth", 4), ("1 Samuel", 31), ("2 Samuel", 24),
    ("1 Kings", 22), ("2 Kings", 25), ("1 Chronicles", 29), ("2 Chronicles", 36),
    ("Ezra", 10), ("Nehemiah", 13), ("Esther", 10),
    ("Job", 42), ("Psalms", 150), ("Proverbs", 31), ("Ecclesiastes", 12), ("Song of Solomon", 8),
    ("Isaiah", 66), ("Jeremiah", 52), ("Lamentations", 5), ("Ezekiel", 48), ("Daniel", 12),
    ("Hosea", 14), ("Joel", 3), ("Amos", 9), ("Obadiah", 1), ("Jonah", 4), ("Micah", 7),
    ("Nahum", 3), ("Habakkuk", 3), ("Zephaniah", 3), ("Haggai", 2), ("Zechariah", 14), ("Malachi", 4),
    ("Matthew", 28), ("Mark", 16), ("Luke", 24), ("John", 21), ("Acts", 28),
    ("Romans", 16), ("1 Corinthians", 16), ("2 Corinthians", 13), ("Galatians", 6),
    ("Ephesians", 6), ("Philippians", 4), ("Colossians", 4), ("1 Thessalonians", 5),
    ("2 Thessalonians", 3), ("1 Timothy", 6), ("2 Timothy", 4), ("Titus", 3), ("Philemon", 1),
    ("Hebrews", 13), ("James", 5), ("1 Peter", 5), ("2 Peter", 3),
    ("1 John", 5), ("2 John", 1), ("3 John", 1), ("Jude", 1), ("Revelation", 22)
]

private let otBooks = Array(bibleBooks[0..<39])  // Genesis–Malachi
private let ntBooks = Array(bibleBooks[39...])   // Matthew–Revelation

// MARK: - Algorithms

private func distribute(_ entries: [ReadingEntry], across days: Int) -> [ReadingPlanDay] {
    let total = entries.count
    var result: [ReadingPlanDay] = []
    for day in 1...days {
        let startIdx = (day - 1) * total / days
        let endIdx = day * total / days
        if endIdx > startIdx {
            result.append(ReadingPlanDay(dayNumber: day, readings: Array(entries[startIdx..<endIdx])))
        }
    }
    return result
}

private func expand(_ segments: [(String, Int, Int)]) -> [ReadingEntry] {
    segments.flatMap { (book, start, end) in
        (start...end).map { ReadingEntry(bookName: book, chapter: $0) }
    }
}

private func chronologicalSequence() -> [ReadingEntry] {
    let segments: [(String, Int, Int)] = [
        ("Genesis", 1, 11), ("Job", 1, 42), ("Genesis", 12, 50),
        ("Exodus", 1, 40), ("Leviticus", 1, 27), ("Numbers", 1, 36), ("Deuteronomy", 1, 34),
        ("Joshua", 1, 24), ("Judges", 1, 21), ("Ruth", 1, 4),
        ("1 Samuel", 1, 31), ("2 Samuel", 1, 24), ("Psalms", 1, 72),
        ("1 Kings", 1, 11), ("Proverbs", 1, 31), ("Ecclesiastes", 1, 12), ("Song of Solomon", 1, 8),
        ("1 Kings", 12, 22), ("2 Kings", 1, 25), ("1 Chronicles", 1, 29), ("2 Chronicles", 1, 36),
        ("Isaiah", 1, 66), ("Jeremiah", 1, 52), ("Lamentations", 1, 5), ("Ezekiel", 1, 48), ("Daniel", 1, 12),
        ("Hosea", 1, 14), ("Joel", 1, 3), ("Amos", 1, 9), ("Obadiah", 1, 1), ("Jonah", 1, 4),
        ("Micah", 1, 7), ("Nahum", 1, 3), ("Habakkuk", 1, 3), ("Zephaniah", 1, 3),
        ("Haggai", 1, 2), ("Zechariah", 1, 14), ("Malachi", 1, 4),
        ("Ezra", 1, 10), ("Nehemiah", 1, 13), ("Esther", 1, 10), ("Psalms", 73, 150),
        ("Matthew", 1, 28), ("Mark", 1, 16), ("Luke", 1, 24), ("John", 1, 21), ("Acts", 1, 28),
        ("Romans", 1, 16), ("1 Corinthians", 1, 16), ("2 Corinthians", 1, 13), ("Galatians", 1, 6),
        ("Ephesians", 1, 6), ("Philippians", 1, 4), ("Colossians", 1, 4),
        ("1 Thessalonians", 1, 5), ("2 Thessalonians", 1, 3), ("1 Timothy", 1, 6), ("2 Timothy", 1, 4),
        ("Titus", 1, 3), ("Philemon", 1, 1), ("Hebrews", 1, 13), ("James", 1, 5),
        ("1 Peter", 1, 5), ("2 Peter", 1, 3), ("1 John", 1, 5), ("2 John", 1, 1), ("3 John", 1, 1),
        ("Jude", 1, 1), ("Revelation", 1, 22)
    ]
    return expand(segments)
}

private func blendedSequence() -> [ReadingPlanDay] {
    let otEntries = otBooks.flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let ntEntries = ntBooks.flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let otDays = distribute(otEntries, across: 365)
    let ntDays = distribute(ntEntries, across: 365)
    return (1...365).map { day in
        let ot = otDays.first(where: { $0.dayNumber == day })?.readings ?? []
        let nt = ntDays.first(where: { $0.dayNumber == day })?.readings ?? []
        return ReadingPlanDay(dayNumber: day, readings: ot + nt)
    }
}

// MARK: - All Reading Plans

let allReadingPlans: [ReadingPlan] = {
    // Sequential — all 1189 chapters in order
    let allEntries = bibleBooks.flatMap { (book, count) in
        (1...count).map { ReadingEntry(bookName: book, chapter: $0) }
    }
    let sequential = ReadingPlan(
        id: "bible-sequential",
        title: "Bible in a Year — Sequential",
        description: "Read through the entire Bible from Genesis to Revelation in one year.",
        category: "Bible in a Year",
        totalDays: 365,
        days: distribute(allEntries, across: 365)
    )

    // Chronological
    let chronological = ReadingPlan(
        id: "bible-chronological",
        title: "Bible in a Year — Chronological",
        description: "Read the Bible in the order events occurred historically.",
        category: "Bible in a Year",
        totalDays: 365,
        days: distribute(chronologicalSequence(), across: 365)
    )

    // Blended
    let blended = ReadingPlan(
        id: "bible-blended",
        title: "Bible in a Year — Blended",
        description: "Each day includes both Old and New Testament readings.",
        category: "Bible in a Year",
        totalDays: 365,
        days: blendedSequence()
    )

    // New Testament in 90 Days
    let ntEntries = ntBooks.flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let nt90 = ReadingPlan(
        id: "new-testament-90",
        title: "New Testament in 90 Days",
        description: "Read through the entire New Testament in just 90 days.",
        category: "New Testament",
        totalDays: 90,
        days: distribute(ntEntries, across: 90)
    )

    // Gospels — Matthew, Mark, Luke, John = 89 chapters
    let gospelBooks = ["Matthew", "Mark", "Luke", "John"]
    let gospelEntries = bibleBooks
        .filter { gospelBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let gospels = ReadingPlan(
        id: "gospels",
        title: "The Gospels",
        description: "Read all four Gospels — Matthew, Mark, Luke, and John.",
        category: "New Testament",
        totalDays: 89,
        days: distribute(gospelEntries, across: 89)
    )

    // Acts & Paul's Letters
    let actsPaulBooks = ["Acts", "Romans", "1 Corinthians", "2 Corinthians", "Galatians",
                         "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
                         "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon"]
    let actsPaulEntries = bibleBooks
        .filter { actsPaulBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let actsPaul = ReadingPlan(
        id: "acts-paul",
        title: "Acts & Paul's Letters",
        description: "Follow Paul's missionary journeys and letters to the early churches.",
        category: "New Testament",
        totalDays: 121,
        days: distribute(actsPaulEntries, across: 121)
    )

    // Letters & Revelation
    let lettersRevBooks = ["Hebrews", "James", "1 Peter", "2 Peter",
                           "1 John", "2 John", "3 John", "Jude", "Revelation"]
    let lettersRevEntries = bibleBooks
        .filter { lettersRevBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let lettersRev = ReadingPlan(
        id: "letters-revelation",
        title: "Letters & Revelation",
        description: "Read Hebrews through Revelation, including the general epistles and Apocalypse.",
        category: "New Testament",
        totalDays: 56,
        days: distribute(lettersRevEntries, across: 56)
    )

    // Torah — Gen, Exo, Lev, Num, Deu = 187 chapters
    let torahBooks = ["Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy"]
    let torahEntries = bibleBooks
        .filter { torahBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let torah = ReadingPlan(
        id: "torah",
        title: "Torah — Five Books of Moses",
        description: "Explore the foundation of Scripture through the Pentateuch.",
        category: "Old Testament",
        totalDays: 94,
        days: distribute(torahEntries, across: 94)
    )

    // Wisdom & Poetry — Job, Proverbs, Ecclesiastes, Song of Solomon = 93 chapters
    let wisdomBooks = ["Job", "Proverbs", "Ecclesiastes", "Song of Solomon"]
    let wisdomEntries = bibleBooks
        .filter { wisdomBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let wisdom = ReadingPlan(
        id: "wisdom-poetry",
        title: "Wisdom & Poetry",
        description: "Journey through Job, Proverbs, Ecclesiastes, and Song of Solomon.",
        category: "Old Testament",
        totalDays: 93,
        days: distribute(wisdomEntries, across: 93)
    )

    // Major Prophets — Isaiah, Jeremiah, Lamentations, Ezekiel, Daniel = 183 chapters
    let majorProphetBooks = ["Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel"]
    let majorProphetEntries = bibleBooks
        .filter { majorProphetBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let majorProphets = ReadingPlan(
        id: "major-prophets",
        title: "The Major Prophets",
        description: "Read Isaiah, Jeremiah, Lamentations, Ezekiel, and Daniel.",
        category: "Old Testament",
        totalDays: 92,
        days: distribute(majorProphetEntries, across: 92)
    )

    // Minor Prophets = 67 chapters
    let minorProphetBooks = ["Hosea", "Joel", "Amos", "Obadiah", "Jonah", "Micah",
                             "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah", "Malachi"]
    let minorProphetEntries = bibleBooks
        .filter { minorProphetBooks.contains($0.0) }
        .flatMap { (book, count) in (1...count).map { ReadingEntry(bookName: book, chapter: $0) } }
    let minorProphets = ReadingPlan(
        id: "minor-prophets",
        title: "The Minor Prophets",
        description: "Read all twelve minor prophets, from Hosea to Malachi.",
        category: "Old Testament",
        totalDays: 34,
        days: distribute(minorProphetEntries, across: 34)
    )

    // Psalms in 30 Days
    let psalmsEntries = (1...150).map { ReadingEntry(bookName: "Psalms", chapter: $0) }
    let psalms30 = ReadingPlan(
        id: "psalms-30",
        title: "Psalms in 30 Days",
        description: "Read all 150 Psalms in 30 days, five psalms each day.",
        category: "Old Testament",
        totalDays: 30,
        days: distribute(psalmsEntries, across: 30)
    )

    return [sequential, chronological, blended,
            nt90, gospels, actsPaul, lettersRev,
            torah, wisdom, majorProphets, minorProphets, psalms30]
}()
