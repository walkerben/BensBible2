import Foundation
import SwiftData

@Model final class MemoryReviewLog {
    var id: UUID = UUID()
    var verseKey: String = ""
    var reviewedAt: Date = Date()
    var quality: Int = 0
    var exerciseType: String = ""

    init(verseKey: String, quality: Int, exerciseType: String) {
        self.verseKey = verseKey
        self.quality = quality
        self.exerciseType = exerciseType
    }
}
