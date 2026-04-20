import Foundation
import UserNotifications

struct ReadingPlanReminderService {
    private let identifier = "reading_plan_reminder"

    func refreshSchedule(hasActivePlan: Bool, hour: Int, minute: Int) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
        guard hasActivePlan else { return }

        let content = UNMutableNotificationContent()
        content.title = "Time for Your Daily Reading"
        content.body = "Your reading plan is waiting. Keep up the great work!"
        content.sound = .default

        var components = DateComponents()
        components.hour = hour
        components.minute = minute
        components.second = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: true)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }

    func cancelNotification() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [identifier])
    }
}
