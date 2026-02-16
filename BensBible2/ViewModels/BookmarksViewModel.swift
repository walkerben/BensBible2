import Foundation

@Observable
final class BookmarksViewModel {
    private var annotationService: AnnotationService?

    var groupedBookmarks: [(book: String, annotations: [VerseAnnotation])] = []

    func configure(annotationService: AnnotationService) {
        self.annotationService = annotationService
    }

    func load() {
        guard let service = annotationService else { return }
        let all = service.fetchAllBookmarks()
        var groups: [(book: String, annotations: [VerseAnnotation])] = []
        var currentBook = ""
        var currentGroup: [VerseAnnotation] = []
        for annotation in all {
            if annotation.bookName != currentBook {
                if !currentGroup.isEmpty {
                    groups.append((book: currentBook, annotations: currentGroup))
                }
                currentBook = annotation.bookName
                currentGroup = [annotation]
            } else {
                currentGroup.append(annotation)
            }
        }
        if !currentGroup.isEmpty {
            groups.append((book: currentBook, annotations: currentGroup))
        }
        groupedBookmarks = groups
    }

    func removeBookmark(_ annotation: VerseAnnotation) {
        annotation.isBookmarked = false
        annotationService?.deleteIfEmpty(annotation)
        load()
    }
}
