package com.bensbible.app.data

import android.content.res.AssetManager
import com.bensbible.app.model.Book
import com.bensbible.app.model.Chapter
import kotlinx.serialization.json.Json

class BibleDataService(private val assetManager: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }
    private var cachedBookNames: List<String>? = null
    private val bookCache = mutableMapOf<String, Book>()

    fun loadBookNames(): List<String> {
        cachedBookNames?.let { return it }
        val text = assetManager.open("bibles/kjv/Books.json").bufferedReader().readText()
        val names = json.decodeFromString<List<String>>(text)
        cachedBookNames = names
        return names
    }

    fun loadBook(name: String): Book {
        bookCache[name]?.let { return it }
        val fileName = name.replace(" ", "")
        val text = assetManager.open("bibles/kjv/$fileName.json").bufferedReader().readText()
        val book = json.decodeFromString<Book>(text)
        bookCache[name] = book
        return book
    }

    fun loadChapter(bookName: String, chapter: Int): Chapter {
        val book = loadBook(bookName)
        return book.chapters.find { it.number == chapter }
            ?: throw IllegalArgumentException("Chapter $chapter not found in $bookName")
    }
}
