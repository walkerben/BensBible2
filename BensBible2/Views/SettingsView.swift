import SwiftUI
import UserNotifications

struct SettingsView: View {
    @AppStorage("verse_of_the_day_enabled") private var isEnabled = false
    @AppStorage("verse_of_the_day_hour") private var notificationHour = 8
    @AppStorage("verse_of_the_day_minute") private var notificationMinute = 0

    @AppStorage("memorize_reminder_enabled") private var memorizeEnabled = false
    @AppStorage("memorize_reminder_hour") private var memorizeHour = 20
    @AppStorage("memorize_reminder_minute") private var memorizeMinute = 0

    @State private var showPermissionDeniedAlert = false

    private let service = VerseOfTheDayService()

    // A Date built from the stored hour/minute for the DatePicker.
    private var notificationTime: Binding<Date> {
        Binding(
            get: {
                var components = Calendar.current.dateComponents([.year, .month, .day], from: Date())
                components.hour = notificationHour
                components.minute = notificationMinute
                return Calendar.current.date(from: components) ?? Date()
            },
            set: { newDate in
                let components = Calendar.current.dateComponents([.hour, .minute], from: newDate)
                notificationHour = components.hour ?? 8
                notificationMinute = components.minute ?? 0
                if isEnabled {
                    service.scheduleNotifications(hour: notificationHour, minute: notificationMinute)
                }
            }
        )
    }

    private var memorizeReminderTime: Binding<Date> {
        Binding(
            get: {
                var components = Calendar.current.dateComponents([.year, .month, .day], from: Date())
                components.hour = memorizeHour
                components.minute = memorizeMinute
                return Calendar.current.date(from: components) ?? Date()
            },
            set: { newDate in
                let components = Calendar.current.dateComponents([.hour, .minute], from: newDate)
                memorizeHour = components.hour ?? 20
                memorizeMinute = components.minute ?? 0
                // MemorizeReminderScheduler in BensBible2App reacts to @AppStorage changes.
            }
        )
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Toggle("Verse of the Day", isOn: Binding(
                        get: { isEnabled },
                        set: { newValue in
                            if newValue {
                                Task { await enableVerseOfTheDay() }
                            } else {
                                isEnabled = false
                                service.cancelNotifications()
                            }
                        }
                    ))

                    if isEnabled {
                        DatePicker(
                            "Notification Time",
                            selection: notificationTime,
                            displayedComponents: .hourAndMinute
                        )
                    }
                } header: {
                    Text("Notifications")
                } footer: {
                    Text("Receive a daily scripture verse as a notification. Tap the notification to open the verse in the reader.")
                }

                Section {
                    Toggle("Memorization Reminder", isOn: Binding(
                        get: { memorizeEnabled },
                        set: { newValue in
                            memorizeEnabled = newValue
                            if !newValue {
                                MemorizeReminderService().cancelNotification()
                            }
                            // When enabled, MemorizeReminderScheduler handles scheduling reactively.
                        }
                    ))

                    if memorizeEnabled {
                        DatePicker(
                            "Reminder Time",
                            selection: memorizeReminderTime,
                            displayedComponents: .hourAndMinute
                        )
                    }
                } header: {
                    Text("Memorization")
                } footer: {
                    Text("Receive a daily reminder to review your memorization verses. Only sent when verses are due.")
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .alert("Notifications Disabled", isPresented: $showPermissionDeniedAlert) {
                Button("Open Settings") {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("To receive the Verse of the Day, please enable notifications for BensBible in Settings.")
            }
        }
    }

    private func enableVerseOfTheDay() async {
        let granted = await service.requestPermission()
        if granted {
            isEnabled = true
            service.scheduleNotifications(hour: notificationHour, minute: notificationMinute)
        } else {
            // Check if previously denied (vs. never asked).
            let settings = await UNUserNotificationCenter.current().notificationSettings()
            if settings.authorizationStatus == .denied {
                showPermissionDeniedAlert = true
            }
            // If .notDetermined the system already showed its own prompt; if denied, show ours.
        }
    }
}
