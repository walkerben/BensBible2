package com.bensbible.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.bensbible.app.data.AppDatabase
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.LocationPreferences
import com.bensbible.app.data.MemorizeRepository
import com.bensbible.app.data.PresentationRepository
import com.bensbible.app.data.VerseOfTheDayPreferences
import com.bensbible.app.workers.VerseOfTheDayWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BensBibleApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    lateinit var annotationRepository: AnnotationRepository
        private set

    lateinit var presentationRepository: PresentationRepository
        private set

    lateinit var memorizeRepository: MemorizeRepository
        private set

    lateinit var bibleDataService: BibleDataService
        private set

    lateinit var locationPreferences: LocationPreferences
        private set

    lateinit var verseOfTheDayPreferences: VerseOfTheDayPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.create(this)
        annotationRepository = AnnotationRepository(database.verseAnnotationDao())
        presentationRepository = PresentationRepository(database.presentationDao())
        memorizeRepository = MemorizeRepository(database.memorizeDao())
        bibleDataService = BibleDataService(assets)
        locationPreferences = LocationPreferences(this)
        verseOfTheDayPreferences = VerseOfTheDayPreferences(this)

        createNotificationChannels()

        applicationScope.launch {
            presentationRepository.seedRomanRoadIfNeeded()
            memorizeRepository.seedDefaultVersesIfNeeded()
        }
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            VerseOfTheDayWorker.CHANNEL_ID,
            "Verse of the Day",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily scripture verse notifications"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
