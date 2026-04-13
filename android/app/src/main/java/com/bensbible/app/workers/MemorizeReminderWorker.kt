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
import com.bensbible.app.data.MemorizeReminderPreferences

class MemorizeReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = MemorizeReminderPreferences(context)
        if (!prefs.isEnabled) return Result.success()

        val app = context.applicationContext as? BensBibleApp ?: return Result.failure()
        val dueCount = app.database.memorizeDao().countDueVerses(System.currentTimeMillis())
        if (dueCount == 0) return Result.success()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_memorize", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val body = if (dueCount == 1)
            "You have 1 verse due for memorization review."
        else
            "You have $dueCount verses due for memorization review."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to Review!")
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "memorize_reminder_channel"
        const val NOTIFICATION_ID = 1002
    }
}
