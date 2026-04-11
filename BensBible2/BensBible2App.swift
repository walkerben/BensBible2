import SwiftUI
import SwiftData
import UserNotifications

// Handles incoming notification responses and surfaces them to the SwiftUI tree via
// the @Observable pendingVerseNavigation property.
@Observable
final class NotificationHandler: NSObject, UNUserNotificationCenterDelegate {
    var pendingVerseNavigation: BibleLocation?

    // Called when the user taps a notification (foreground or background).
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let info = response.notification.request.content.userInfo
        if let book = info["book"] as? String,
           let chapter = info["chapter"] as? Int,
           let verse = info["verse"] as? Int {
            DispatchQueue.main.async {
                self.pendingVerseNavigation = BibleLocation(
                    bookName: book,
                    chapterNumber: chapter,
                    verseNumber: verse
                )
            }
        }
        completionHandler()
    }

    // Allow the notification banner to show while the app is in the foreground.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }
}

@main
struct BensBible2App: App {
    @State private var coordinator = NavigationCoordinator()
    @State private var showSplash = true
    @State private var notificationHandler = NotificationHandler()
    @AppStorage("verse_of_the_day_enabled") private var votdEnabled = false
    @AppStorage("verse_of_the_day_hour") private var votdHour = 8
    @AppStorage("verse_of_the_day_minute") private var votdMinute = 0

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

                    SearchView()
                        .tabItem {
                            Label("Search", systemImage: "magnifyingglass")
                        }
                        .tag(AppTab.search)

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

                    PresentationsView()
                        .tabItem {
                            Label("Present", systemImage: "play.rectangle")
                        }
                        .tag(AppTab.present)

                    MemorizeView()
                        .tabItem {
                            Label("Memorize", systemImage: "brain")
                        }
                        .tag(AppTab.memorize)
                }
                .environment(coordinator)

                if showSplash {
                    SplashScreenView()
                        .transition(.opacity)
                        .zIndex(1)
                }
            }
            .onAppear {
                // Register notification delegate.
                UNUserNotificationCenter.current().delegate = notificationHandler

                // Refresh the rolling 60-day notification window each time the app launches.
                if votdEnabled {
                    VerseOfTheDayService().scheduleNotifications(hour: votdHour, minute: votdMinute)
                }

                DispatchQueue.main.asyncAfter(deadline: .now() + 2.8) {
                    withAnimation(.easeOut(duration: 0.5)) {
                        showSplash = false
                    }
                }
            }
            // Navigate to verse when the user taps a Verse of the Day notification.
            .onChange(of: notificationHandler.pendingVerseNavigation) { _, location in
                if let location {
                    coordinator.navigateToReader(location: location)
                    notificationHandler.pendingVerseNavigation = nil
                }
            }
        }
        .modelContainer(for: [VerseAnnotation.self, Presentation.self, PresentationSlide.self, MemorizedVerse.self, MemoryReviewLog.self])
    }
}
