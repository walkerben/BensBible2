import Foundation

struct BibleLocation: Equatable, Codable {
    var bookName: String
    var chapterNumber: Int
    var verseNumber: Int?

    static let genesis1 = BibleLocation(bookName: "Genesis", chapterNumber: 1)

    var displayTitle: String {
        "\(bookName) \(chapterNumber)"
    }
}
