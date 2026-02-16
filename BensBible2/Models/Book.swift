import Foundation

struct Book: Codable {
    let book: String
    let chapters: [Chapter]
}
