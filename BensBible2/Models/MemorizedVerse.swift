import Foundation
import SwiftData

@Model final class MemorizedVerse {
    var id: UUID = UUID()
    var bookName: String = ""
    var chapterNumber: Int = 0
    var verseNumber: Int = 0
    var verseText: String = ""

    // SM-2 scheduling fields
    var repetitions: Int = 0
    var easeFactor: Double = 2.5
    var intervalDays: Int = 1
    var nextReviewDate: Date = Date()

    // Metadata
    var addedAt: Date = Date()
    var lastReviewedAt: Date? = nil
    var totalReviews: Int = 0

    init(bookName: String, chapterNumber: Int, verseNumber: Int, verseText: String) {
        self.bookName = bookName
        self.chapterNumber = chapterNumber
        self.verseNumber = verseNumber
        self.verseText = verseText
    }

    var verseKey: String { "\(bookName) \(chapterNumber):\(verseNumber)" }
    var reference: String { "\(bookName) \(chapterNumber):\(verseNumber)" }
    var isDue: Bool { nextReviewDate <= Date() }
}
