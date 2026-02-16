import Foundation
import SwiftData

protocol AnnotationService {
    func fetchAnnotations(book: String, chapter: Int) -> [String: VerseAnnotation]
    func annotationFor(verseID: VerseID) -> VerseAnnotation?
    func setHighlight(color: HighlightColor?, for verseIDs: [VerseID])
    func toggleBookmark(for verseIDs: [VerseID])
    func saveNote(text: String, for verseID: VerseID)
    func deleteIfEmpty(_ annotation: VerseAnnotation)
    func fetchAllBookmarks() -> [VerseAnnotation]
    func fetchAllNotes() -> [VerseAnnotation]
}

final class SwiftDataAnnotationService: AnnotationService {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func fetchAnnotations(book: String, chapter: Int) -> [String: VerseAnnotation] {
        let predicate = #Predicate<VerseAnnotation> {
            $0.bookName == book && $0.chapterNumber == chapter
        }
        let descriptor = FetchDescriptor<VerseAnnotation>(predicate: predicate)
        let results = (try? modelContext.fetch(descriptor)) ?? []
        var dict: [String: VerseAnnotation] = [:]
        for annotation in results {
            dict[annotation.verseKey] = annotation
        }
        return dict
    }

    func annotationFor(verseID: VerseID) -> VerseAnnotation? {
        let key = verseID.key
        let predicate = #Predicate<VerseAnnotation> { $0.verseKey == key }
        var descriptor = FetchDescriptor<VerseAnnotation>(predicate: predicate)
        descriptor.fetchLimit = 1
        return try? modelContext.fetch(descriptor).first
    }

    func setHighlight(color: HighlightColor?, for verseIDs: [VerseID]) {
        for verseID in verseIDs {
            let annotation = findOrCreate(verseID: verseID)
            annotation.highlightColor = color
            deleteIfEmpty(annotation)
        }
        save()
    }

    func toggleBookmark(for verseIDs: [VerseID]) {
        let annotations = verseIDs.map { findOrCreate(verseID: $0) }
        let allBookmarked = annotations.allSatisfy { $0.isBookmarked }
        for annotation in annotations {
            annotation.isBookmarked = !allBookmarked
            deleteIfEmpty(annotation)
        }
        save()
    }

    func saveNote(text: String, for verseID: VerseID) {
        let annotation = findOrCreate(verseID: verseID)
        annotation.noteText = text.isEmpty ? nil : text
        deleteIfEmpty(annotation)
        save()
    }

    func deleteIfEmpty(_ annotation: VerseAnnotation) {
        if annotation.isEmpty {
            modelContext.delete(annotation)
        }
    }

    func fetchAllBookmarks() -> [VerseAnnotation] {
        let predicate = #Predicate<VerseAnnotation> { $0.isBookmarked == true }
        let descriptor = FetchDescriptor<VerseAnnotation>(
            predicate: predicate,
            sortBy: [
                SortDescriptor(\.bookName),
                SortDescriptor(\.chapterNumber),
                SortDescriptor(\.verseNumber)
            ]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func fetchAllNotes() -> [VerseAnnotation] {
        let predicate = #Predicate<VerseAnnotation> { $0.noteText != nil }
        let descriptor = FetchDescriptor<VerseAnnotation>(
            predicate: predicate,
            sortBy: [
                SortDescriptor(\.bookName),
                SortDescriptor(\.chapterNumber),
                SortDescriptor(\.verseNumber)
            ]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    // MARK: - Private

    private func findOrCreate(verseID: VerseID) -> VerseAnnotation {
        if let existing = annotationFor(verseID: verseID) {
            return existing
        }
        let annotation = VerseAnnotation(verseID: verseID)
        modelContext.insert(annotation)
        return annotation
    }

    private func save() {
        try? modelContext.save()
    }
}
