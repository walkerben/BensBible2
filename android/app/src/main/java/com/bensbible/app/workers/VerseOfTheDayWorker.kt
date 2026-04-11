package com.bensbible.app.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bensbible.app.MainActivity
import com.bensbible.app.data.VerseOfTheDayPreferences
import com.bensbible.app.data.verseOfTheDayVerses
import java.util.Calendar

class VerseOfTheDayWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = VerseOfTheDayPreferences(context)
        if (!prefs.isEnabled) return Result.success()

        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = ((dayOfYear - 1) % verseOfTheDayVerses.size)
        val entry = verseOfTheDayVerses[index]

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("verse_of_the_day_book", entry.bookName)
            putExtra("verse_of_the_day_chapter", entry.chapter)
            putExtra("verse_of_the_day_verse", entry.verse)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val reference = "${entry.bookName} ${entry.chapter}:${entry.verse}"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Verse of the Day — $reference")
            .setContentText(entry.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(entry.text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        return Result.success()
    }

    companion object {
        const val CHANNEL_ID = "verse_of_the_day_channel"
        const val NOTIFICATION_ID = 1001
    }
}
