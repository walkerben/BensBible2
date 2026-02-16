import Foundation

@Observable
final class NotesViewModel {
    private var annotationService: AnnotationService?

    var notes: [VerseAnnotation] = []
    var isEditorPresented = false
    var editingAnnotation: VerseAnnotation?
    var editingText: String = ""

    func configure(annotationService: AnnotationService) {
        self.annotationService = annotationService
    }

    func load() {
        guard let service = annotationService else { return }
        notes = service.fetchAllNotes()
    }

    func beginEditing(_ annotation: VerseAnnotation) {
        editingAnnotation = annotation
        editingText = annotation.noteText ?? ""
        isEditorPresented = true
    }

    func saveEdit() {
        guard let annotation = editingAnnotation else { return }
        let verseID = VerseID(book: annotation.bookName, chapter: annotation.chapterNumber, verse: annotation.verseNumber)
        annotationService?.saveNote(text: editingText, for: verseID)
        editingAnnotation = nil
        editingText = ""
        load()
    }

    func cancelEdit() {
        editingAnnotation = nil
        editingText = ""
    }

    func deleteNote(_ annotation: VerseAnnotation) {
        let verseID = VerseID(book: annotation.bookName, chapter: annotation.chapterNumber, verse: annotation.verseNumber)
        annotationService?.saveNote(text: "", for: verseID)
        load()
    }
}
