package com.bensbible.app.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bensbible.app.BensBibleApp
import com.bensbible.app.MainActivity
import com.bensbible.app.data.ReadingPlanReminderPreferences

class ReadingPlanReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = ReadingPlanReminderPreferences(context)
        if (!prefs.isEnabled) return Result.success()

        val activePlans = (context.applicationContext as BensBibleApp)
            .readingPlanRepository.countActivePlans()
        if (activePlans == 0) return Result.success()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_reading_plan", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time for Your Daily Reading")
            .setContentText("Your reading plan is waiting. Keep up the great work!")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "reading_plan_reminder_channel"
        const val NOTIFICATION_ID = 1003
    }
}
