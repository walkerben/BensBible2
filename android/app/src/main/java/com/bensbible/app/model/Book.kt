package com.bensbible.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Book(
    val book: String,
    val chapters: List<Chapter>
)
