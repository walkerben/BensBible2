import SwiftUI

struct SearchView: View {
    @State private var viewModel = SearchViewModel()
    @Environment(NavigationCoordinator.self) private var coordinator

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(BookGroup.searchFilters) { group in
                            Button {
                                viewModel.selectGroup(group)
                            } label: {
                                Text(group.displayName)
                                    .font(.subheadline)
                                    .padding(.horizontal, 12)
                                    .padding(.vertical, 6)
                                    .background(
                                        Capsule()
                                            .fill(viewModel.selectedGroup == group
                                                  ? Color.accentColor
                                                  : Color(.systemGray5))
                                    )
                                    .foregroundStyle(viewModel.selectedGroup == group
                                                     ? .white
                                                     : .primary)
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }
                Group {
                if viewModel.query.trimmingCharacters(in: .whitespaces).isEmpty {
                    ContentUnavailableView(
                        "Search the Bible",
                        systemImage: "magnifyingglass",
                        description: Text("Search for words or phrases across all 66 books.")
                    )
                } else if viewModel.isSearching {
                    ProgressView("Searching...")
                } else if viewModel.results.isEmpty {
                    ContentUnavailableView(
                        "No Results",
                        systemImage: "magnifyingglass",
                        description: Text("No results for \"\(viewModel.query)\"")
                    )
                } else {
                    List(viewModel.results) { result in
                        Button {
                            coordinator.navigateToReader(
                                location: BibleLocation(
                                    bookName: result.bookName,
                                    chapterNumber: result.chapter,
                                    verseNumber: result.verse
                                )
                            )
                        } label: {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(result.reference)
                                    .font(.subheadline)
                                    .fontWeight(.bold)
                                    .foregroundStyle(.primary)
                                Text(result.text)
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                    .lineLimit(2)
                            }
                        }
                    }
                }
                }
            }
            .navigationTitle("Search")
            .searchable(text: $viewModel.query, prompt: "Search the Bible")
        }
    }
}
