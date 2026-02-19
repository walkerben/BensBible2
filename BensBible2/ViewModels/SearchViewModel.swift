import Foundation

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
    var isSearching: Bool = false
    var selectedGroup: BookGroup = .all

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

    private func debounceSearch() {
        searchTask?.cancel()
        let currentQuery = query.trimmingCharacters(in: .whitespaces)

        guard !currentQuery.isEmpty else {
            results = []
            isSearching = false
            return
        }

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
            let bookNames = selectedGroup.filterBooks(from: allBooks)
            var matches: [SearchResult] = []

            for bookName in bookNames {
                if Task.isCancelled { return }
                let book = try dataService.loadBook(named: bookName)
                for chapter in book.chapters {
                    for verse in chapter.verses {
                        if verse.text.localizedCaseInsensitiveContains(query) {
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
                self.isSearching = false
            }
        } catch {
            await MainActor.run {
                self.results = []
                self.isSearching = false
            }
        }
    }
}
