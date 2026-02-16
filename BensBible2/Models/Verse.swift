import Foundation

struct Verse: Codable, Identifiable {
    let verse: String
    let text: String

    var id: String { verse }

    var number: Int {
        Int(verse) ?? 0
    }
}
