package com.bensbible.app.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bensbible.app.data.MemorizeReminderPreferences
import com.bensbible.app.data.VerseOfTheDayPreferences
import com.bensbible.app.workers.MemorizeReminderWorker
import com.bensbible.app.workers.VerseOfTheDayWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SettingsViewModel(
    private val votdPreferences: VerseOfTheDayPreferences,
    private val memorizePreferences: MemorizeReminderPreferences
) {

    // --- Verse of the Day ---

    var isVerseOfTheDayEnabled by mutableStateOf(votdPreferences.isEnabled)
        private set

    var notificationHour by mutableIntStateOf(votdPreferences.notificationHour)
        private set

    var notificationMinute by mutableIntStateOf(votdPreferences.notificationMinute)
        private set

    fun setVerseOfTheDayEnabled(enabled: Boolean, context: Context) {
        votdPreferences.isEnabled = enabled
        isVerseOfTheDayEnabled = enabled
        if (enabled) scheduleVerseOfTheDay(context) else cancelVerseOfTheDay(context)
    }

    fun setNotificationTime(hour: Int, minute: Int, context: Context) {
        votdPreferences.notificationHour = hour
        votdPreferences.notificationMinute = minute
        notificationHour = hour
        notificationMinute = minute
        if (isVerseOfTheDayEnabled) scheduleVerseOfTheDay(context)
    }

    fun scheduleVerseOfTheDay(context: Context) {
        schedulePeriodicWork<VerseOfTheDayWorker>(context, notificationHour, notificationMinute, VOTD_WORK_NAME)
    }

    fun cancelVerseOfTheDay(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(VOTD_WORK_NAME)
    }

    // --- Memorization Reminder ---

    var isMemorizeReminderEnabled by mutableStateOf(memorizePreferences.isEnabled)
        private set

    var memorizeReminderHour by mutableIntStateOf(memorizePreferences.notificationHour)
        private set

    var memorizeReminderMinute by mutableIntStateOf(memorizePreferences.notificationMinute)
        private set

    fun setMemorizeReminderEnabled(enabled: Boolean, context: Context) {
        memorizePreferences.isEnabled = enabled
        isMemorizeReminderEnabled = enabled
        if (enabled) scheduleMemorizeReminder(context) else cancelMemorizeReminder(context)
    }

    fun setMemorizeReminderTime(hour: Int, minute: Int, context: Context) {
        memorizePreferences.notificationHour = hour
        memorizePreferences.notificationMinute = minute
        memorizeReminderHour = hour
        memorizeReminderMinute = minute
        if (isMemorizeReminderEnabled) scheduleMemorizeReminder(context)
    }

    fun scheduleMemorizeReminder(context: Context) {
        schedulePeriodicWork<MemorizeReminderWorker>(context, memorizeReminderHour, memorizeReminderMinute, MEMORIZE_WORK_NAME)
    }

    fun cancelMemorizeReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(MEMORIZE_WORK_NAME)
    }

    // --- Shared scheduling helper ---

    private inline fun <reified W : androidx.work.ListenableWorker> schedulePeriodicWork(
        context: Context,
        hour: Int,
        minute: Int,
        workName: String
    ) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<W>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    companion object {
        const val VOTD_WORK_NAME = "VerseOfTheDayWork"
        const val MEMORIZE_WORK_NAME = "MemorizeReminderWork"
    }
}
