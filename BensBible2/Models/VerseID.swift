import Foundation

struct VerseID: Hashable, Equatable, CustomStringConvertible {
    let book: String
    let chapter: Int
    let verse: Int

    var key: String { "\(book):\(chapter):\(verse)" }
    var description: String { key }

    var displayReference: String {
        "\(book) \(chapter):\(verse)"
    }

    init(book: String, chapter: Int, verse: Int) {
        self.book = book
        self.chapter = chapter
        self.verse = verse
    }

    init?(key: String) {
        let parts = key.split(separator: ":")
        guard parts.count == 3,
              let chapter = Int(parts[1]),
              let verse = Int(parts[2]) else { return nil }
        self.book = String(parts[0])
        self.chapter = chapter
        self.verse = verse
    }
}
