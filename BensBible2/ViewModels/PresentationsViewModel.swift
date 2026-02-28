import Foundation
import SwiftData
import Observation

@Observable
final class PresentationsViewModel {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    func load() -> [Presentation] {
        let descriptor = FetchDescriptor<Presentation>(
            sortBy: [SortDescriptor(\.createdAt, order: .reverse)]
        )
        return (try? modelContext.fetch(descriptor)) ?? []
    }

    func create(name: String) {
        let presentation = Presentation(name: name)
        modelContext.insert(presentation)
    }

    func delete(_ presentation: Presentation) {
        modelContext.delete(presentation)
    }

    func seedRomanRoadIfNeeded() {
        let descriptor = FetchDescriptor<Presentation>()
        let count = (try? modelContext.fetchCount(descriptor)) ?? 0
        guard count == 0 else { return }

        let presentation = Presentation(name: "Roman Road")
        modelContext.insert(presentation)

        let romanRoad: [(book: String, chapter: Int, verse: Int, text: String)] = [
            ("Romans", 3, 23, "For all have sinned, and come short of the glory of God;"),
            ("Romans", 6, 23, "For the wages of sin is death; but the gift of God is eternal life through Jesus Christ our Lord."),
            ("Romans", 5, 8, "But God commendeth his love toward us, in that, while we were yet sinners, Christ died for us."),
            ("Romans", 10, 9, "That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved."),
            ("Romans", 10, 10, "For with the heart man believeth unto righteousness; and with the mouth confession is made unto salvation."),
            ("Romans", 10, 13, "For whosoever shall call upon the name of the Lord shall be saved.")
        ]

        for (index, entry) in romanRoad.enumerated() {
            let slide = PresentationSlide(
                bookName: entry.book,
                chapterNumber: entry.chapter,
                verseNumber: entry.verse,
                verseText: entry.text,
                order: index
            )
            slide.presentation = presentation
            modelContext.insert(slide)
        }
    }
}
