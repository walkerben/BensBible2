package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.VerseAnnotationEntity
import kotlinx.coroutines.launch

data class BookmarkGroup(
    val book: String,
    val annotations: List<VerseAnnotationEntity>
)

class BookmarksViewModel(
    private val repository: AnnotationRepository
) : ViewModel() {

    var groupedBookmarks by mutableStateOf<List<BookmarkGroup>>(emptyList())
        private set

    fun load() {
        viewModelScope.launch {
            val all = repository.fetchAllBookmarks()
            val groups = mutableListOf<BookmarkGroup>()
            var currentBook = ""
            var currentGroup = mutableListOf<VerseAnnotationEntity>()
            for (annotation in all) {
                if (annotation.bookName != currentBook) {
                    if (currentGroup.isNotEmpty()) {
                        groups.add(BookmarkGroup(currentBook, currentGroup.toList()))
                    }
                    currentBook = annotation.bookName
                    currentGroup = mutableListOf(annotation)
                } else {
                    currentGroup.add(annotation)
                }
            }
            if (currentGroup.isNotEmpty()) {
                groups.add(BookmarkGroup(currentBook, currentGroup.toList()))
            }
            groupedBookmarks = groups
        }
    }

    fun removeBookmark(annotation: VerseAnnotationEntity) {
        viewModelScope.launch {
            repository.removeBookmark(annotation)
            load()
        }
    }
}
