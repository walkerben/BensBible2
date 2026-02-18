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

    static func displayRange(from verseIDs: Set<VerseID>) -> String {
        guard let first = verseIDs.first else { return "" }

        let book = first.book
        let chapter = first.chapter
        let verses = verseIDs.map(\.verse).sorted()

        var ranges: [String] = []
        var rangeStart = verses[0]
        var rangeEnd = verses[0]

        for i in 1..<verses.count {
            if verses[i] == rangeEnd + 1 {
                rangeEnd = verses[i]
            } else {
                ranges.append(rangeStart == rangeEnd ? "\(rangeStart)" : "\(rangeStart)-\(rangeEnd)")
                rangeStart = verses[i]
                rangeEnd = verses[i]
            }
        }
        ranges.append(rangeStart == rangeEnd ? "\(rangeStart)" : "\(rangeStart)-\(rangeEnd)")

        return "\(book) \(chapter):\(ranges.joined(separator: ", "))"
    }
}
