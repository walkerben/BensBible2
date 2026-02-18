package com.bensbible.app.model

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle

enum class ShareFont(
    val displayName: String,
    val fontFamily: FontFamily,
    val fontStyle: FontStyle = FontStyle.Normal
) {
    SERIF("Serif", FontFamily.Serif),
    SERIF_ITALIC("Serif Italic", FontFamily.Serif, FontStyle.Italic),
    SANS_SERIF("Sans", FontFamily.SansSerif),
    SANS_ITALIC("Sans Italic", FontFamily.SansSerif, FontStyle.Italic),
    MONOSPACE("Mono", FontFamily.Monospace);
}
