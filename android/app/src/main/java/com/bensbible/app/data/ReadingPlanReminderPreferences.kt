package com.bensbible.app.data

import android.content.Context

class ReadingPlanReminderPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("reading_plan_reminder_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = prefs.getBoolean("enabled", false)
        set(value) { prefs.edit().putBoolean("enabled", value).apply() }

    var notificationHour: Int
        get() = prefs.getInt("hour", 7)
        set(value) { prefs.edit().putInt("hour", value).apply() }

    var notificationMinute: Int
        get() = prefs.getInt("minute", 0)
        set(value) { prefs.edit().putInt("minute", value).apply() }
}
