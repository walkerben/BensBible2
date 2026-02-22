import SwiftUI
import SwiftData

struct BookmarksView: View {
    @State private var viewModel = BookmarksViewModel()
    @Environment(NavigationCoordinator.self) private var coordinator
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.groupedBookmarks.isEmpty {
                    ContentUnavailableView(
                        "No Bookmarks",
                        systemImage: "bookmark",
                        description: Text("Bookmarked verses will appear here.")
                    )
                } else {
                    List {
                        ForEach(viewModel.groupedBookmarks, id: \.book) { group in
                            Section(group.book) {
                                ForEach(group.annotations, id: \.verseKey) { annotation in
                                    Button {
                                        coordinator.navigateToReader(
                                            location: BibleLocation(
                                                bookName: annotation.bookName,
                                                chapterNumber: annotation.chapterNumber,
                                                verseNumber: annotation.verseNumber
                                            )
                                        )
                                    } label: {
                                        Text("\(annotation.bookName) \(annotation.chapterNumber):\(annotation.verseNumber)")
                                            .foregroundStyle(.primary)
                                    }
                                    .swipeActions(edge: .trailing) {
                                        Button(role: .destructive) {
                                            viewModel.removeBookmark(annotation)
                                        } label: {
                                            Label("Remove", systemImage: "bookmark.slash")
                                        }
                                    }
                                    .contextMenu {
                                        Button(role: .destructive) {
                                            viewModel.removeBookmark(annotation)
                                        } label: {
                                            Label("Remove Bookmark", systemImage: "bookmark.slash")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Bookmarks")
        }
        .onAppear {
            viewModel.configure(annotationService: SwiftDataAnnotationService(modelContext: modelContext))
            viewModel.load()
        }
    }
}
