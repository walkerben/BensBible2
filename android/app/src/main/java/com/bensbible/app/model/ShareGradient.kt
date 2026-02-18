package com.bensbible.app.model

import androidx.compose.ui.graphics.Color

enum class ShareGradient {
    NAVY,
    SUNSET,
    FOREST,
    PARCHMENT,
    MIDNIGHT;

    val colors: List<Color>
        get() = when (this) {
            NAVY -> listOf(Color(0.05f, 0.1f, 0.3f), Color(0.15f, 0.25f, 0.55f))
            SUNSET -> listOf(Color(0.95f, 0.5f, 0.2f), Color(0.85f, 0.25f, 0.4f))
            FOREST -> listOf(Color(0.05f, 0.2f, 0.15f), Color(0.1f, 0.4f, 0.35f))
            PARCHMENT -> listOf(Color(0.96f, 0.93f, 0.85f), Color(0.88f, 0.82f, 0.7f))
            MIDNIGHT -> listOf(Color(0.05f, 0.02f, 0.1f), Color(0.2f, 0.08f, 0.35f))
        }

    val textColor: Color
        get() = when (this) {
            PARCHMENT -> Color(0.2f, 0.15f, 0.1f)
            else -> Color.White
        }

    val accentColor: Color
        get() = when (this) {
            NAVY -> Color(0.6f, 0.75f, 1.0f)
            SUNSET -> Color(1.0f, 0.9f, 0.7f)
            FOREST -> Color(0.5f, 0.85f, 0.7f)
            PARCHMENT -> Color(0.55f, 0.4f, 0.25f)
            MIDNIGHT -> Color(0.7f, 0.55f, 0.95f)
        }
}
