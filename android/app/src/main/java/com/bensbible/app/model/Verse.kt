package com.bensbible.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Verse(
    val verse: String,
    val text: String
) {
    val number: Int get() = verse.toIntOrNull() ?: 0
}
