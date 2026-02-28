package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.model.BookGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SearchMode { PHRASE, ALL_WORDS }

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

    var selectedGroup by mutableStateOf(BookGroup.ALL)
        private set

    var searchMode by mutableStateOf(SearchMode.PHRASE)
        private set

    private var searchJob: Job? = null

    fun onSearchModeChange(mode: SearchMode) {
        searchMode = mode
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        isSearching = true
        searchJob = viewModelScope.launch {
            performSearch(trimmed)
        }
    }

    fun onGroupChange(group: BookGroup) {
        selectedGroup = group
        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        isSearching = true
        searchJob = viewModelScope.launch {
            performSearch(trimmed)
        }
    }

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
            val allBooks = dataService.loadBookNames()
            val bookNames = selectedGroup.filterBooks(allBooks)
            val found = mutableListOf<SearchResult>()
            for (bookName in bookNames) {
                val book = dataService.loadBook(bookName)
                for (chapter in book.chapters) {
                    for (verse in chapter.verses) {
                        val matched = when (searchMode) {
                            SearchMode.PHRASE -> verse.text.contains(query, ignoreCase = true)
                            SearchMode.ALL_WORDS -> query.trim().split("\\s+".toRegex())
                                .all { word -> verse.text.contains(word, ignoreCase = true) }
                        }
                        if (matched) {
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
