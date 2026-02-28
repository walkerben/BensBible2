import Foundation
import SwiftData

@Model final class PresentationSlide {
    var id: UUID = UUID()
    var bookName: String
    var chapterNumber: Int
    var verseNumber: Int
    var verseText: String
    var order: Int
    var presentation: Presentation?

    init(bookName: String, chapterNumber: Int, verseNumber: Int, verseText: String, order: Int) {
        self.bookName = bookName
        self.chapterNumber = chapterNumber
        self.verseNumber = verseNumber
        self.verseText = verseText
        self.order = order
    }

    var reference: String {
        "\(bookName) \(chapterNumber):\(verseNumber)"
    }
}
