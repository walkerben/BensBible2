package com.bensbible.app.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.bensbible.app.data.VerseOfTheDayPreferences
import com.bensbible.app.workers.VerseOfTheDayWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SettingsViewModel(private val preferences: VerseOfTheDayPreferences) {

    var isVerseOfTheDayEnabled by mutableStateOf(preferences.isEnabled)
        private set

    var notificationHour by mutableIntStateOf(preferences.notificationHour)
        private set

    var notificationMinute by mutableIntStateOf(preferences.notificationMinute)
        private set

    fun setVerseOfTheDayEnabled(enabled: Boolean, context: Context) {
        preferences.isEnabled = enabled
        isVerseOfTheDayEnabled = enabled
        if (enabled) {
            scheduleVerseOfTheDay(context)
        } else {
            cancelVerseOfTheDay(context)
        }
    }

    fun setNotificationTime(hour: Int, minute: Int, context: Context) {
        preferences.notificationHour = hour
        preferences.notificationMinute = minute
        notificationHour = hour
        notificationMinute = minute
        if (isVerseOfTheDayEnabled) {
            scheduleVerseOfTheDay(context)
        }
    }

    fun scheduleVerseOfTheDay(context: Context) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notificationHour)
            set(Calendar.MINUTE, notificationMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<VerseOfTheDayWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun cancelVerseOfTheDay(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "VerseOfTheDayWork"
    }
}
