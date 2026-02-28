import Foundation
import SwiftData

@Model final class Presentation {
    var id: UUID = UUID()
    var name: String
    var createdAt: Date = Date()
    @Relationship(deleteRule: .cascade) var slides: [PresentationSlide] = []

    init(name: String) {
        self.name = name
    }
}
