import SwiftUI
import SwiftData
import UserNotifications

// Handles incoming notification responses and surfaces them to the SwiftUI tree via
// the @Observable pendingVerseNavigation property.
@Observable
final class NotificationHandler: NSObject, UNUserNotificationCenterDelegate {
    var pendingVerseNavigation: BibleLocation?
    var pendingMemorizeNavigation = false
    var pendingReadingPlanNavigation = false

    // Called when the user taps a notification (foreground or background).
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let identifier = response.notification.request.identifier
        let info = response.notification.request.content.userInfo

        if identifier == "memorize_reminder" {
            DispatchQueue.main.async { self.pendingMemorizeNavigation = true }
        } else if identifier == "reading_plan_reminder" {
            DispatchQueue.main.async { self.pendingReadingPlanNavigation = true }
        } else if let book = info["book"] as? String,
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

// Reactively reschedules the reading plan reminder whenever active plans or preferences change.
private struct ReadingPlanReminderScheduler: View {
    @Query private var allProgress: [ReadingPlanProgress]
    @AppStorage("reading_plan_reminder_enabled") private var enabled = false
    @AppStorage("reading_plan_reminder_hour") private var hour = 7
    @AppStorage("reading_plan_reminder_minute") private var minute = 0

    private var hasActivePlan: Bool { allProgress.contains { !$0.isCompleted } }

    var body: some View {
        Color.clear
            .onAppear { refresh() }
            .onChange(of: hasActivePlan) { _, _ in refresh() }
            .onChange(of: enabled) { _, _ in refresh() }
            .onChange(of: hour) { _, _ in refresh() }
            .onChange(of: minute) { _, _ in refresh() }
    }

    private func refresh() {
        let service = ReadingPlanReminderService()
        guard enabled else {
            service.cancelNotification()
            return
        }
        service.refreshSchedule(hasActivePlan: hasActivePlan, hour: hour, minute: minute)
    }
}

// Embedded in the WindowGroup ZStack to reactively reschedule the memorize
// reminder whenever the due-verse count or user preferences change.
private struct MemorizeReminderScheduler: View {
    @Query private var allVerses: [MemorizedVerse]
    @AppStorage("memorize_reminder_enabled") private var enabled = false
    @AppStorage("memorize_reminder_hour") private var hour = 20
    @AppStorage("memorize_reminder_minute") private var minute = 0

    private var dueCount: Int { allVerses.filter(\.isDue).count }

    var body: some View {
        Color.clear
            .onAppear { refresh() }
            .onChange(of: dueCount) { _, _ in refresh() }
            .onChange(of: enabled) { _, _ in refresh() }
            .onChange(of: hour) { _, _ in refresh() }
            .onChange(of: minute) { _, _ in refresh() }
    }

    private func refresh() {
        let service = MemorizeReminderService()
        guard enabled else {
            service.cancelNotification()
            return
        }
        service.refreshSchedule(dueCount: dueCount, hour: hour, minute: minute)
    }
}

@main
struct BensBible2App: App {
    @State private var coordinator = NavigationCoordinator()
    @State private var showSplash = true
    @State private var notificationHandler = NotificationHandler()
    @State private var bibleDataService = LocalBibleDataService()
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

                    ReadingPlanListView(bibleDataService: bibleDataService)
                        .tabItem {
                            Label("Plans", systemImage: "calendar")
                        }
                        .tag(AppTab.readingPlan)
                }
                .environment(coordinator)

                MemorizeReminderScheduler()
                ReadingPlanReminderScheduler()

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
            // Switch to Memorize tab when the user taps a memorization reminder.
            .onChange(of: notificationHandler.pendingMemorizeNavigation) { _, pending in
                if pending {
                    coordinator.selectedTab = .memorize
                    notificationHandler.pendingMemorizeNavigation = false
                }
            }
            // Switch to Reading Plans tab when the user taps a reading plan reminder.
            .onChange(of: notificationHandler.pendingReadingPlanNavigation) { _, pending in
                if pending {
                    coordinator.selectedTab = .readingPlan
                    notificationHandler.pendingReadingPlanNavigation = false
                }
            }
        }
        .modelContainer(for: [VerseAnnotation.self, Presentation.self, PresentationSlide.self, MemorizedVerse.self, MemoryReviewLog.self, ReadingPlanProgress.self])
    }
}
