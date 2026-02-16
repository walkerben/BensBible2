import Foundation

enum BibleDataError: Error, LocalizedError {
    case bookNotFound(String)
    case chapterNotFound(String, Int)
    case fileNotFound(String)
    case decodingFailed(String)

    var errorDescription: String? {
        switch self {
        case .bookNotFound(let name):
            "Book not found: \(name)"
        case .chapterNotFound(let book, let chapter):
            "Chapter \(chapter) not found in \(book)"
        case .fileNotFound(let filename):
            "File not found: \(filename)"
        case .decodingFailed(let detail):
            "Failed to decode: \(detail)"
        }
    }
}

protocol BibleDataService {
    func loadBookNames() throws -> [String]
    func loadBook(named name: String) throws -> Book
}

extension BibleDataService {
    func loadChapter(bookName: String, chapter: Int) throws -> Chapter {
        let book = try loadBook(named: bookName)
        guard let ch = book.chapters.first(where: { $0.number == chapter }) else {
            throw BibleDataError.chapterNotFound(bookName, chapter)
        }
        return ch
    }
}
