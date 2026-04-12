import Foundation
import UserNotifications

struct MemorizeReminderService {
    private let identifier = "memorize_reminder"

    /// Cancels any pending reminder, then reschedules for the next occurrence of
    /// hour:minute only if dueCount > 0.
    func refreshSchedule(dueCount: Int, hour: Int, minute: Int) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
        guard dueCount > 0 else { return }

        let content = UNMutableNotificationContent()
        content.title = "Time to Review!"
        content.body = dueCount == 1
            ? "You have 1 verse due for memorization review."
            : "You have \(dueCount) verses due for memorization review."
        content.sound = .default

        // UNCalendarNotificationTrigger with only hour/minute fires at the next
        // matching clock time — today if it hasn't passed yet, otherwise tomorrow.
        var components = DateComponents()
        components.hour = hour
        components.minute = minute
        components.second = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }

    func cancelNotification() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
    }
}
