package com.bensbible.app

import android.app.Application
import com.bensbible.app.data.AppDatabase
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService

class BensBibleApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var annotationRepository: AnnotationRepository
        private set

    lateinit var bibleDataService: BibleDataService
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.create(this)
        annotationRepository = AnnotationRepository(database.verseAnnotationDao())
        bibleDataService = BibleDataService(assets)
    }
}
