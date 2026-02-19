import Foundation

enum BookGroup: String, CaseIterable, Identifiable {
    case all
    case oldTestament
    case newTestament
    case law
    case history
    case poetry
    case prophets
    case gospels
    case acts
    case epistles
    case revelation

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .all: "All"
        case .oldTestament: "Old Testament"
        case .newTestament: "New Testament"
        case .law: "Law"
        case .history: "History"
        case .poetry: "Poetry"
        case .prophets: "Prophets"
        case .gospels: "Gospels"
        case .acts: "Acts"
        case .epistles: "Epistles"
        case .revelation: "Revelation"
        }
    }

    var bookIndexRange: ClosedRange<Int> {
        switch self {
        case .all: 0...65
        case .oldTestament: 0...38
        case .newTestament: 39...65
        case .law: 0...4
        case .history: 5...16
        case .poetry: 17...21
        case .prophets: 22...38
        case .gospels: 39...42
        case .acts: 43...43
        case .epistles: 44...64
        case .revelation: 65...65
        }
    }

    func filterBooks(from allBooks: [String]) -> [String] {
        let range = bookIndexRange
        let start = max(range.lowerBound, 0)
        let end = min(range.upperBound, allBooks.count - 1)
        guard start <= end, start < allBooks.count else { return [] }
        return Array(allBooks[start...end])
    }

    static let pickerSections: [BookGroup] = [
        .law, .history, .poetry, .prophets, .gospels, .acts, .epistles, .revelation
    ]

    static let searchFilters: [BookGroup] = BookGroup.allCases
}
