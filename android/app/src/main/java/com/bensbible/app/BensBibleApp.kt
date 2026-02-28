package com.bensbible.app

import android.app.Application
import com.bensbible.app.data.AppDatabase
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.PresentationRepository
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

    lateinit var bibleDataService: BibleDataService
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.create(this)
        annotationRepository = AnnotationRepository(database.verseAnnotationDao())
        presentationRepository = PresentationRepository(database.presentationDao())
        bibleDataService = BibleDataService(assets)

        applicationScope.launch {
            presentationRepository.seedRomanRoadIfNeeded()
        }
    }
}
