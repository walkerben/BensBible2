package com.bensbible.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val chapter: String,
    val verses: List<Verse>
) {
    val number: Int get() = chapter.toIntOrNull() ?: 0
}
