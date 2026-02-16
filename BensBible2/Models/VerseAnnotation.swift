import Foundation
import SwiftData

@Model
final class VerseAnnotation {
    @Attribute(.unique) var verseKey: String
    var bookName: String
    var chapterNumber: Int
    var verseNumber: Int
    var highlightColorRaw: String?
    var noteText: String?
    var isBookmarked: Bool

    init(verseID: VerseID) {
        self.verseKey = verseID.key
        self.bookName = verseID.book
        self.chapterNumber = verseID.chapter
        self.verseNumber = verseID.verse
        self.isBookmarked = false
    }

    var highlightColor: HighlightColor? {
        get {
            guard let raw = highlightColorRaw else { return nil }
            return HighlightColor(rawValue: raw)
        }
        set {
            highlightColorRaw = newValue?.rawValue
        }
    }

    var isEmpty: Bool {
        highlightColorRaw == nil && (noteText == nil || noteText?.isEmpty == true) && !isBookmarked
    }
}
