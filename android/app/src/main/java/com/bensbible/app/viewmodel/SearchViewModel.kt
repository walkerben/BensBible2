package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.BibleDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SearchResult(
    val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String
) {
    val reference: String get() = "$bookName $chapter:$verse"
}

class SearchViewModel(
    private val dataService: BibleDataService
) : ViewModel() {

    var query by mutableStateOf("")
        private set

    var results by mutableStateOf<List<SearchResult>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)
        private set

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()

        val trimmed = newQuery.trim()
        if (trimmed.isEmpty()) {
            results = emptyList()
            isSearching = false
            return
        }

        isSearching = true
        searchJob = viewModelScope.launch {
            delay(300)
            performSearch(trimmed)
        }
    }

    private suspend fun performSearch(query: String) {
        val matches = withContext(Dispatchers.Default) {
            val bookNames = dataService.loadBookNames()
            val found = mutableListOf<SearchResult>()
            for (bookName in bookNames) {
                val book = dataService.loadBook(bookName)
                for (chapter in book.chapters) {
                    for (verse in chapter.verses) {
                        if (verse.text.contains(query, ignoreCase = true)) {
                            found.add(
                                SearchResult(
                                    bookName = bookName,
                                    chapter = chapter.number,
                                    verse = verse.number,
                                    text = verse.text
                                )
                            )
                        }
                    }
                }
            }
            found
        }
        results = matches
        isSearching = false
    }
}
