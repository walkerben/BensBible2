import Foundation

final class LocalBibleDataService: BibleDataService {
    private var cachedBooks: [String: Book] = [:]
    private var cachedBookNames: [String]?

    func loadBookNames() throws -> [String] {
        if let cached = cachedBookNames {
            return cached
        }

        guard let url = Bundle.main.url(forResource: "Books", withExtension: "json", subdirectory: "assets/bibles/kjv") else {
            throw BibleDataError.fileNotFound("Books.json")
        }

        let data = try Data(contentsOf: url)
        let names = try JSONDecoder().decode([String].self, from: data)
        cachedBookNames = names
        return names
    }

    func loadBook(named name: String) throws -> Book {
        if let cached = cachedBooks[name] {
            return cached
        }

        let filename = name.replacingOccurrences(of: " ", with: "")

        guard let url = Bundle.main.url(forResource: filename, withExtension: "json", subdirectory: "assets/bibles/kjv") else {
            throw BibleDataError.fileNotFound("\(filename).json")
        }

        let data = try Data(contentsOf: url)
        let book: Book
        do {
            book = try JSONDecoder().decode(Book.self, from: data)
        } catch {
            throw BibleDataError.decodingFailed(error.localizedDescription)
        }

        cachedBooks[name] = book
        return book
    }
}
