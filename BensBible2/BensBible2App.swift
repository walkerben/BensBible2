import SwiftUI
import SwiftData

@main
struct BensBible2App: App {
    @State private var coordinator = NavigationCoordinator()

    var body: some Scene {
        WindowGroup {
            TabView(selection: Binding(
                get: { coordinator.selectedTab },
                set: { coordinator.selectedTab = $0 }
            )) {
                ReaderView()
                    .tabItem {
                        Label("Read", systemImage: "book")
                    }
                    .tag(AppTab.read)

                BookmarksView()
                    .tabItem {
                        Label("Bookmarks", systemImage: "bookmark")
                    }
                    .tag(AppTab.bookmarks)

                NotesView()
                    .tabItem {
                        Label("Notes", systemImage: "note.text")
                    }
                    .tag(AppTab.notes)
            }
            .environment(coordinator)
        }
        .modelContainer(for: VerseAnnotation.self)
    }
}
