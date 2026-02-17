import SwiftUI
import SwiftData

@main
struct BensBible2App: App {
    @State private var coordinator = NavigationCoordinator()
    @State private var showSplash = true

    var body: some Scene {
        WindowGroup {
            ZStack {
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

                if showSplash {
                    SplashScreenView()
                        .transition(.opacity)
                        .zIndex(1)
                }
            }
            .onAppear {
                DispatchQueue.main.asyncAfter(deadline: .now() + 2.8) {
                    withAnimation(.easeOut(duration: 0.5)) {
                        showSplash = false
                    }
                }
            }
        }
        .modelContainer(for: VerseAnnotation.self)
    }
}
