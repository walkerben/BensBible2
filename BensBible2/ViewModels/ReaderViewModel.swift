import Foundation
import SwiftUI

@Observable
final class ReaderViewModel {
    private let dataService: BibleDataService

    var bookNames: [String] = []
    var currentLocation: BibleLocation = .genesis1
    var currentChapter: Chapter?
    var currentBookChapterCount: Int = 0
    var isPickerPresented = false
    var scrollToVerseID: String?
    var highlightedVerseID: String?
    var errorMessage: String?

    // MARK: - Selection & Annotations

    var selectedVerseIDs: Set<VerseID> = []
    var chapterAnnotations: [String: VerseAnnotation] = [:]
    var annotationService: AnnotationService?

    var isHighlightPickerPresented = false
    var isNoteEditorPresented = false
    var isShareSheetPresented = false
    var noteEditingVerseID: VerseID?
    var noteEditingText: String = ""

    var hasSelection: Bool { !selectedVerseIDs.isEmpty }
    var selectedCount: Int { selectedVerseIDs.count }

    var selectedVerseTexts: [(number: Int, text: String)] {
        guard let chapter = currentChapter else { return [] }
        let selectedNumbers = selectedVerseIDs.map(\.verse)
        return chapter.verses
            .filter { selectedNumbers.contains($0.number) }
            .sorted { $0.number < $1.number }
            .map { (number: $0.number, text: $0.text) }
    }

    var selectedVerseReference: String {
        VerseID.displayRange(from: selectedVerseIDs)
    }

    init(dataService: BibleDataService = LocalBibleDataService()) {
        self.dataService = dataService
    }

    func configure(annotationService: AnnotationService) {
        self.annotationService = annotationService
    }

    func onAppear() {
        guard bookNames.isEmpty else { return }
        do {
            bookNames = try dataService.loadBookNames()
            try loadCurrentChapter()
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func navigateTo(book: String, chapter: Int, verse: Int? = nil) {
        currentLocation = BibleLocation(bookName: book, chapterNumber: chapter, verseNumber: verse)
        deselectAll()
        do {
            try loadCurrentChapter()
            if let verse {
                scrollToVerseID = String(verse)
                highlightedVerseID = String(verse)
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func nextChapter() {
        if currentLocation.chapterNumber < currentBookChapterCount {
            navigateTo(book: currentLocation.bookName, chapter: currentLocation.chapterNumber + 1)
        } else if let nextBook = bookAfter(currentLocation.bookName) {
            navigateTo(book: nextBook, chapter: 1)
        }
    }

    func previousChapter() {
        if currentLocation.chapterNumber > 1 {
            navigateTo(book: currentLocation.bookName, chapter: currentLocation.chapterNumber - 1)
        } else if let prevBook = bookBefore(currentLocation.bookName) {
            let count = chapterCount(for: prevBook)
            navigateTo(book: prevBook, chapter: count)
        }
    }

    var canGoNext: Bool {
        if currentLocation.chapterNumber < currentBookChapterCount { return true }
        return bookAfter(currentLocation.bookName) != nil
    }

    var canGoPrevious: Bool {
        if currentLocation.chapterNumber > 1 { return true }
        return bookBefore(currentLocation.bookName) != nil
    }

    func chapterCount(for bookName: String) -> Int {
        do {
            let book = try dataService.loadBook(named: bookName)
            return book.chapters.count
        } catch {
            return 0
        }
    }

    // MARK: - Verse Selection

    func verseID(for verse: Verse) -> VerseID {
        VerseID(book: currentLocation.bookName, chapter: currentLocation.chapterNumber, verse: verse.number)
    }

    func toggleVerseSelection(_ verse: Verse) {
        let id = verseID(for: verse)
        if selectedVerseIDs.contains(id) {
            selectedVerseIDs.remove(id)
        } else {
            selectedVerseIDs.insert(id)
        }
    }

    func isSelected(_ verse: Verse) -> Bool {
        selectedVerseIDs.contains(verseID(for: verse))
    }

    func deselectAll() {
        selectedVerseIDs.removeAll()
    }

    func annotation(for verse: Verse) -> VerseAnnotation? {
        let id = verseID(for: verse)
        return chapterAnnotations[id.key]
    }

    // MARK: - Annotation Actions

    func applyHighlight(_ color: HighlightColor?) {
        guard !selectedVerseIDs.isEmpty else { return }
        annotationService?.setHighlight(color: color, for: Array(selectedVerseIDs))
        loadAnnotationsForCurrentChapter()
        deselectAll()
    }

    func bookmarkSelectedVerses() {
        guard !selectedVerseIDs.isEmpty else { return }
        annotationService?.toggleBookmark(for: Array(selectedVerseIDs))
        loadAnnotationsForCurrentChapter()
        deselectAll()
    }

    func beginNoteEditing() {
        guard let firstID = selectedVerseIDs.sorted(by: { $0.verse < $1.verse }).first else { return }
        noteEditingVerseID = firstID
        noteEditingText = chapterAnnotations[firstID.key]?.noteText ?? ""
        isNoteEditorPresented = true
    }

    func saveNote() {
        guard let verseID = noteEditingVerseID else { return }
        annotationService?.saveNote(text: noteEditingText, for: verseID)
        loadAnnotationsForCurrentChapter()
        noteEditingVerseID = nil
        noteEditingText = ""
        deselectAll()
    }

    func cancelNoteEditing() {
        noteEditingVerseID = nil
        noteEditingText = ""
    }

    // MARK: - Private

    private func loadCurrentChapter() throws {
        let chapter = try dataService.loadChapter(
            bookName: currentLocation.bookName,
            chapter: currentLocation.chapterNumber
        )
        currentChapter = chapter
        let book = try dataService.loadBook(named: currentLocation.bookName)
        currentBookChapterCount = book.chapters.count
        loadAnnotationsForCurrentChapter()
    }

    func loadAnnotationsForCurrentChapter() {
        guard let service = annotationService else { return }
        chapterAnnotations = service.fetchAnnotations(
            book: currentLocation.bookName,
            chapter: currentLocation.chapterNumber
        )
    }

    private func bookAfter(_ name: String) -> String? {
        guard let index = bookNames.firstIndex(of: name),
              index + 1 < bookNames.count else { return nil }
        return bookNames[index + 1]
    }

    private func bookBefore(_ name: String) -> String? {
        guard let index = bookNames.firstIndex(of: name),
              index > 0 else { return nil }
        return bookNames[index - 1]
    }
}
