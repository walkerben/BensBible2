import Foundation

struct Chapter: Codable, Identifiable {
    let chapter: String
    let verses: [Verse]

    var id: String { chapter }

    var number: Int {
        Int(chapter) ?? 0
    }
}
