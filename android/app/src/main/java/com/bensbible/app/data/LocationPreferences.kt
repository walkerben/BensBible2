package com.bensbible.app.data

import android.content.Context
import com.bensbible.app.model.BibleLocation

class LocationPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    fun save(location: BibleLocation) {
        prefs.edit()
            .putString("book", location.bookName)
            .putInt("chapter", location.chapterNumber)
            .apply()
    }

    fun load(): BibleLocation {
        val book = prefs.getString("book", null) ?: return BibleLocation.genesis1
        val chapter = prefs.getInt("chapter", 1)
        return BibleLocation(bookName = book, chapterNumber = chapter)
    }
}
