package com.bensbible.app.data

import com.bensbible.app.model.HighlightColor
import com.bensbible.app.model.VerseID

class AnnotationRepository(private val dao: VerseAnnotationDao) {

    suspend fun fetchAnnotations(book: String, chapter: Int): Map<String, VerseAnnotationEntity> {
        return dao.fetchByBookAndChapter(book, chapter).associateBy { it.verseKey }
    }

    suspend fun setHighlight(color: HighlightColor?, verseIDs: List<VerseID>) {
        for (verseID in verseIDs) {
            val annotation = findOrCreate(verseID)
            val updated = annotation.copy(highlightColorRaw = color?.rawValue)
            if (updated.isEmpty) {
                dao.delete(updated)
            } else {
                dao.upsert(updated)
            }
        }
    }

    suspend fun toggleBookmark(verseIDs: List<VerseID>) {
        val annotations = verseIDs.map { findOrCreate(it) }
        val allBookmarked = annotations.all { it.isBookmarked }
        for (annotation in annotations) {
            val updated = annotation.copy(isBookmarked = !allBookmarked)
            if (updated.isEmpty) {
                dao.delete(updated)
            } else {
                dao.upsert(updated)
            }
        }
    }

    suspend fun saveNote(text: String, verseID: VerseID) {
        val annotation = findOrCreate(verseID)
        val noteText = text.ifEmpty { null }
        val updated = annotation.copy(noteText = noteText)
        if (updated.isEmpty) {
            dao.delete(updated)
        } else {
            dao.upsert(updated)
        }
    }

    suspend fun removeBookmark(annotation: VerseAnnotationEntity) {
        val current = dao.fetchByKey(annotation.verseKey) ?: return
        val updated = current.copy(isBookmarked = false)
        if (updated.isEmpty) {
            dao.delete(updated)
        } else {
            dao.upsert(updated)
        }
    }

    suspend fun deleteNote(annotation: VerseAnnotationEntity) {
        val current = dao.fetchByKey(annotation.verseKey) ?: return
        val updated = current.copy(noteText = null)
        if (updated.isEmpty) {
            dao.delete(updated)
        } else {
            dao.upsert(updated)
        }
    }

    suspend fun fetchAllBookmarks(): List<VerseAnnotationEntity> {
        return dao.fetchAllBookmarks()
    }

    suspend fun fetchAllNotes(): List<VerseAnnotationEntity> {
        return dao.fetchAllNotes()
    }

    private suspend fun findOrCreate(verseID: VerseID): VerseAnnotationEntity {
        return dao.fetchByKey(verseID.key) ?: VerseAnnotationEntity.create(verseID)
    }
}
