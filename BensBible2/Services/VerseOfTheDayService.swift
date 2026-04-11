import Foundation
import UserNotifications

final class VerseOfTheDayService {

    static let notificationIdentifierPrefix = "verse_of_the_day_"

    // Returns the entry for the current calendar day-of-year (1–366).
    // Falls back to entry 1 if today's day number has no match.
    func todaysVerse() -> VerseOfTheDayEntry {
        let dayOfYear = Calendar.current.ordinality(of: .day, in: .year, for: Date()) ?? 1
        return verseOfTheDayVerses.first { $0.dayOfYear == dayOfYear }
            ?? verseOfTheDayVerses[0]
    }

    func requestPermission() async -> Bool {
        let center = UNUserNotificationCenter.current()
        do {
            return try await center.requestAuthorization(options: [.alert, .sound, .badge])
        } catch {
            return false
        }
    }

    /// Schedule the next 60 days of Verse of the Day notifications, replacing any existing ones.
    /// Needs to be called after permission is granted and again each time the app opens
    /// (to keep the rolling 60-day window fresh).
    func scheduleNotifications(hour: Int, minute: Int) {
        let center = UNUserNotificationCenter.current()

        // Remove existing Verse of the Day notifications before rescheduling.
        center.getPendingNotificationRequests { [self] requests in
            let ids = requests
                .filter { $0.identifier.hasPrefix(Self.notificationIdentifierPrefix) }
                .map { $0.identifier }
            center.removePendingNotificationRequests(withIdentifiers: ids)

            let calendar = Calendar.current
            let today = Date()

            for offset in 0..<60 {
                guard let date = calendar.date(byAdding: .day, value: offset, to: today) else { continue }
                let dayOfYear = calendar.ordinality(of: .day, in: .year, for: date) ?? 1
                let entry = verseOfTheDayVerses.first { $0.dayOfYear == dayOfYear }
                    ?? verseOfTheDayVerses[0]

                var components = calendar.dateComponents([.year, .month, .day], from: date)
                components.hour = hour
                components.minute = minute
                components.second = 0

                let content = UNMutableNotificationContent()
                content.title = "Verse of the Day"
                content.body = "\(entry.bookName) \(entry.chapter):\(entry.verse) — \(entry.text)"
                content.sound = .default
                content.userInfo = [
                    "book": entry.bookName,
                    "chapter": entry.chapter,
                    "verse": entry.verse
                ]

                let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
                let request = UNNotificationRequest(
                    identifier: "\(Self.notificationIdentifierPrefix)\(offset)",
                    content: content,
                    trigger: trigger
                )
                center.add(request)
            }
        }
    }

    func cancelNotifications() {
        let center = UNUserNotificationCenter.current()
        center.getPendingNotificationRequests { requests in
            let ids = requests
                .filter { $0.identifier.hasPrefix(Self.notificationIdentifierPrefix) }
                .map { $0.identifier }
            center.removePendingNotificationRequests(withIdentifiers: ids)
        }
    }
}
