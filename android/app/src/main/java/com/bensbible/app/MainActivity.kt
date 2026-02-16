package com.bensbible.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bensbible.app.ui.MainScreen
import com.bensbible.app.ui.theme.BensBibleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as BensBibleApp
        setContent {
            BensBibleTheme {
                MainScreen(
                    bibleDataService = app.bibleDataService,
                    annotationRepository = app.annotationRepository
                )
            }
        }
    }
}
