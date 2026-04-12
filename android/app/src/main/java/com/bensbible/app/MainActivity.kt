package com.bensbible.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.bensbible.app.model.BibleLocation
import com.bensbible.app.ui.MainScreen
import com.bensbible.app.ui.theme.BensBibleTheme

class MainActivity : ComponentActivity() {

    private var pendingVerseNavigation by mutableStateOf<BibleLocation?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingVerseNavigation = extractVerseFromIntent(intent)
        val app = application as BensBibleApp
        setContent {
            BensBibleTheme {
                MainScreen(
                    bibleDataService = app.bibleDataService,
                    annotationRepository = app.annotationRepository,
                    presentationRepository = app.presentationRepository,
                    memorizeRepository = app.memorizeRepository,
                    locationPreferences = app.locationPreferences,
                    verseOfTheDayPreferences = app.verseOfTheDayPreferences,
                    memorizeReminderPreferences = app.memorizeReminderPreferences,
                    initialNavigation = pendingVerseNavigation,
                    onInitialNavigationConsumed = { pendingVerseNavigation = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingVerseNavigation = extractVerseFromIntent(intent)
    }

    private fun extractVerseFromIntent(intent: Intent?): BibleLocation? {
        val book = intent?.getStringExtra("verse_of_the_day_book") ?: return null
        val chapter = intent.getIntExtra("verse_of_the_day_chapter", -1).takeIf { it != -1 } ?: return null
        val verse = intent.getIntExtra("verse_of_the_day_verse", -1).takeIf { it != -1 } ?: return null
        return BibleLocation(bookName = book, chapterNumber = chapter, verseNumber = verse)
    }
}
