package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.BibleDataService
import com.bensbible.app.data.VerseAnnotationEntity
import com.bensbible.app.model.BibleLocation
import com.bensbible.app.model.Chapter
import com.bensbible.app.model.HighlightColor
import com.bensbible.app.model.Verse
import com.bensbible.app.model.VerseID
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val dataService: BibleDataService,
    private val repository: AnnotationRepository
) : ViewModel() {

    var bookNames by mutableStateOf<List<String>>(emptyList())
        private set

    var currentLocation by mutableStateOf(BibleLocation.genesis1)
        private set

    var currentChapter by mutableStateOf<Chapter?>(null)
        private set

    var currentBookChapterCount by mutableIntStateOf(0)
        private set

    var selectedVerseIDs by mutableStateOf<Set<VerseID>>(emptySet())
        private set

    val chapterAnnotations = mutableStateMapOf<String, VerseAnnotationEntity>()

    var isPickerPresented by mutableStateOf(false)
    var isHighlightPickerPresented by mutableStateOf(false)
    var isShareSheetPresented by mutableStateOf(false)
    var isNoteEditorPresented by mutableStateOf(false)
    var noteEditingVerseID by mutableStateOf<VerseID?>(null)
        private set
    var noteEditingText by mutableStateOf("")
    var scrollToVerseID by mutableStateOf<String?>(null)
    var highlightedVerseID by mutableStateOf<String?>(null)

    val hasSelection: Boolean get() = selectedVerseIDs.isNotEmpty()
    val selectedCount: Int get() = selectedVerseIDs.size

    val selectedVerseTexts: List<Pair<Int, String>>
        get() {
            val chapter = currentChapter ?: return emptyList()
            val sorted = selectedVerseIDs.sorted()
            return sorted.mapNotNull { id ->
                chapter.verses.find { it.number == id.verse }?.let { verse ->
                    verse.number to verse.text
                }
            }
        }

    val selectedVerseReference: String
        get() = VerseID.displayRange(selectedVerseIDs)

    val canGoNext: Boolean
        get() {
            if (currentLocation.chapterNumber < currentBookChapterCount) return true
            return bookAfter(currentLocation.bookName) != null
        }

    val canGoPrevious: Boolean
        get() {
            if (currentLocation.chapterNumber > 1) return true
            return bookBefore(currentLocation.bookName) != null
        }

    fun onAppear() {
        if (bookNames.isNotEmpty()) return
        try {
            bookNames = dataService.loadBookNames()
            loadCurrentChapter()
        } catch (e: Exception) {
            // Log error
        }
    }

    fun navigateTo(book: String, chapter: Int, verse: Int? = null) {
        currentLocation = BibleLocation(bookName = book, chapterNumber = chapter, verseNumber = verse)
        deselectAll()
        try {
            loadCurrentChapter()
            if (verse != null) {
                scrollToVerseID = verse.toString()
                highlightedVerseID = verse.toString()
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    fun nextChapter() {
        if (currentLocation.chapterNumber < currentBookChapterCount) {
            navigateTo(currentLocation.bookName, currentLocation.chapterNumber + 1)
        } else {
            bookAfter(currentLocation.bookName)?.let { navigateTo(it, 1) }
        }
    }

    fun previousChapter() {
        if (currentLocation.chapterNumber > 1) {
            navigateTo(currentLocation.bookName, currentLocation.chapterNumber - 1)
        } else {
            bookBefore(currentLocation.bookName)?.let { prev ->
                val count = chapterCount(prev)
                navigateTo(prev, count)
            }
        }
    }

    fun chapterCount(bookName: String): Int {
        return try {
            dataService.loadBook(bookName).chapters.size
        } catch (e: Exception) {
            0
        }
    }

    fun verseID(verse: Verse): VerseID {
        return VerseID(
            book = currentLocation.bookName,
            chapter = currentLocation.chapterNumber,
            verse = verse.number
        )
    }

    fun toggleVerseSelection(verse: Verse) {
        val id = verseID(verse)
        selectedVerseIDs = if (selectedVerseIDs.contains(id)) {
            selectedVerseIDs - id
        } else {
            selectedVerseIDs + id
        }
    }

    fun isSelected(verse: Verse): Boolean {
        return selectedVerseIDs.contains(verseID(verse))
    }

    fun deselectAll() {
        selectedVerseIDs = emptySet()
    }

    fun annotation(verse: Verse): VerseAnnotationEntity? {
        return chapterAnnotations[verseID(verse).key]
    }

    fun applyHighlight(color: HighlightColor?) {
        if (selectedVerseIDs.isEmpty()) return
        val ids = selectedVerseIDs.toList()
        viewModelScope.launch {
            repository.setHighlight(color, ids)
            loadAnnotationsForCurrentChapter()
        }
        deselectAll()
    }

    fun bookmarkSelectedVerses() {
        if (selectedVerseIDs.isEmpty()) return
        val ids = selectedVerseIDs.toList()
        viewModelScope.launch {
            repository.toggleBookmark(ids)
            loadAnnotationsForCurrentChapter()
        }
        deselectAll()
    }

    fun beginNoteEditing() {
        val firstID = selectedVerseIDs.minByOrNull { it.verse } ?: return
        noteEditingVerseID = firstID
        noteEditingText = chapterAnnotations[firstID.key]?.noteText ?: ""
        isNoteEditorPresented = true
    }

    fun saveNote() {
        val verseID = noteEditingVerseID ?: return
        val text = noteEditingText
        viewModelScope.launch {
            repository.saveNote(text, verseID)
            loadAnnotationsForCurrentChapter()
        }
        noteEditingVerseID = null
        noteEditingText = ""
        deselectAll()
    }

    fun cancelNoteEditing() {
        noteEditingVerseID = null
        noteEditingText = ""
    }

    fun loadAnnotationsForCurrentChapter() {
        viewModelScope.launch {
            val annotations = repository.fetchAnnotations(
                currentLocation.bookName,
                currentLocation.chapterNumber
            )
            chapterAnnotations.clear()
            chapterAnnotations.putAll(annotations)
        }
    }

    private fun loadCurrentChapter() {
        val chapter = dataService.loadChapter(
            currentLocation.bookName,
            currentLocation.chapterNumber
        )
        currentChapter = chapter
        val book = dataService.loadBook(currentLocation.bookName)
        currentBookChapterCount = book.chapters.size
        loadAnnotationsForCurrentChapter()
    }

    private fun bookAfter(name: String): String? {
        val index = bookNames.indexOf(name)
        return if (index >= 0 && index + 1 < bookNames.size) bookNames[index + 1] else null
    }

    private fun bookBefore(name: String): String? {
        val index = bookNames.indexOf(name)
        return if (index > 0) bookNames[index - 1] else null
    }
}
