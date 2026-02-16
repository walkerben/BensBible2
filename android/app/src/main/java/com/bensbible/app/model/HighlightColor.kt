package com.bensbible.app.model

import androidx.compose.ui.graphics.Color

enum class HighlightColor(val rawValue: String) {
    YELLOW("yellow"),
    GREEN("green"),
    BLUE("blue"),
    PINK("pink"),
    ORANGE("orange");

    val color: Color
        get() = when (this) {
            YELLOW -> Color(0x54FFEB3B)
            GREEN -> Color(0x4D4CAF50)
            BLUE -> Color(0x4D2196F3)
            PINK -> Color(0x4DE91E63)
            ORANGE -> Color(0x54FF9800)
        }

    val solidColor: Color
        get() = when (this) {
            YELLOW -> Color(0xFFFFEB3B)
            GREEN -> Color(0xFF4CAF50)
            BLUE -> Color(0xFF2196F3)
            PINK -> Color(0xFFE91E63)
            ORANGE -> Color(0xFFFF9800)
        }

    val displayName: String get() = rawValue.replaceFirstChar { it.uppercase() }

    companion object {
        fun fromRawValue(raw: String?): HighlightColor? {
            if (raw == null) return null
            return entries.find { it.rawValue == raw }
        }
    }
}
