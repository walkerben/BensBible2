package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.AnnotationRepository
import com.bensbible.app.data.VerseAnnotationEntity
import com.bensbible.app.model.VerseID
import kotlinx.coroutines.launch

class NotesViewModel(
    private val repository: AnnotationRepository
) : ViewModel() {

    var notes by mutableStateOf<List<VerseAnnotationEntity>>(emptyList())
        private set

    var isEditorPresented by mutableStateOf(false)
    var editingAnnotation by mutableStateOf<VerseAnnotationEntity?>(null)
        private set
    var editingText by mutableStateOf("")

    fun load() {
        viewModelScope.launch {
            notes = repository.fetchAllNotes()
        }
    }

    fun beginEditing(annotation: VerseAnnotationEntity) {
        editingAnnotation = annotation
        editingText = annotation.noteText ?: ""
        isEditorPresented = true
    }

    fun saveEdit() {
        val annotation = editingAnnotation ?: return
        val verseID = VerseID(
            book = annotation.bookName,
            chapter = annotation.chapterNumber,
            verse = annotation.verseNumber
        )
        val text = editingText
        viewModelScope.launch {
            repository.saveNote(text, verseID)
            editingAnnotation = null
            editingText = ""
            load()
        }
    }

    fun cancelEdit() {
        editingAnnotation = null
        editingText = ""
    }

    fun deleteNote(annotation: VerseAnnotationEntity) {
        viewModelScope.launch {
            repository.deleteNote(annotation)
            load()
        }
    }
}
