import Foundation

enum SearchMode: String, CaseIterable {
    case phrase = "Phrase"
    case allWords = "All Words"
}

struct SearchResult: Identifiable {
    let id = UUID()
    let bookName: String
    let chapter: Int
    let verse: Int
    let text: String

    var reference: String { "\(bookName) \(chapter):\(verse)" }
}

@Observable
final class SearchViewModel {
    private let dataService: BibleDataService
    private var searchTask: Task<Void, Never>?

    var query: String = "" {
        didSet { debounceSearch() }
    }
    var results: [SearchResult] = []
    var navigationTarget: BibleLocation? = nil
    var isSearching: Bool = false
    var selectedGroup: BookGroup = .all
    var searchMode: SearchMode = .phrase {
        didSet { triggerSearch() }
    }

    init(dataService: BibleDataService = LocalBibleDataService()) {
        self.dataService = dataService
    }

    func selectGroup(_ group: BookGroup) {
        selectedGroup = group
        searchTask?.cancel()
        let currentQuery = query.trimmingCharacters(in: .whitespaces)
        guard !currentQuery.isEmpty else { return }
        isSearching = true
        searchTask = Task {
            await performSearch(query: currentQuery)
        }
    }

    private func triggerSearch() {
        searchTask?.cancel()
        let currentQuery = query.trimmingCharacters(in: .whitespaces)
        guard !currentQuery.isEmpty else { return }
        isSearching = true
        searchTask = Task {
            await performSearch(query: currentQuery)
        }
    }

    private func debounceSearch() {
        searchTask?.cancel()
        let currentQuery = query.trimmingCharacters(in: .whitespaces)

        guard !currentQuery.isEmpty else {
            results = []
            navigationTarget = nil
            isSearching = false
            return
        }

        navigationTarget = nil
        isSearching = true
        searchTask = Task {
            try? await Task.sleep(nanoseconds: 300_000_000)
            guard !Task.isCancelled else { return }
            await performSearch(query: currentQuery)
        }
    }

    private func performSearch(query: String) async {
        do {
            let allBooks = try dataService.loadBookNames()
            let navTarget = parseNavigationTarget(query: query, allBooks: allBooks)
            let bookNames = selectedGroup.filterBooks(from: allBooks)
            var matches: [SearchResult] = []

            for bookName in bookNames {
                if Task.isCancelled { return }
                let book = try dataService.loadBook(named: bookName)
                for chapter in book.chapters {
                    for verse in chapter.verses {
                        let matched: Bool
                        switch searchMode {
                        case .phrase:
                            matched = verse.text.localizedCaseInsensitiveContains(query)
                        case .allWords:
                            let words = query.split(separator: " ").map(String.init)
                            matched = words.allSatisfy { verse.text.localizedCaseInsensitiveContains($0) }
                        }
                        if matched {
                            matches.append(SearchResult(
                                bookName: bookName,
                                chapter: chapter.number,
                                verse: verse.number,
                                text: verse.text
                            ))
                        }
                    }
                }
            }

            let finalMatches = matches
            guard !Task.isCancelled else { return }
            await MainActor.run {
                self.results = finalMatches
                self.navigationTarget = navTarget
                self.isSearching = false
            }
        } catch {
            await MainActor.run {
                self.results = []
                self.navigationTarget = nil
                self.isSearching = false
            }
        }
    }

    private func parseNavigationTarget(query: String, allBooks: [String]) -> BibleLocation? {
        let parts = query.trimmingCharacters(in: .whitespaces)
            .components(separatedBy: .whitespaces).filter { !$0.isEmpty }
        guard parts.count >= 2 else { return nil }

        let lastPart = parts.last!
        let cv = lastPart.components(separatedBy: ":")
        guard let chapter = Int(cv[0]), chapter > 0 else { return nil }
        let verse = cv.count > 1 ? Int(cv[1]) : nil

        let potentialBook = parts.dropLast().joined(separator: " ")
        let aliases = ["Psalm": "Psalms"]
        let resolvedBook = aliases[potentialBook] ?? potentialBook

        guard let book = allBooks.first(where: {
            $0.localizedCaseInsensitiveCompare(resolvedBook) == .orderedSame
        }) else { return nil }

        return BibleLocation(bookName: book, chapterNumber: chapter, verseNumber: verse)
    }
}
