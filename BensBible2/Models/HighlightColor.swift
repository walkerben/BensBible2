import SwiftUI

enum HighlightColor: String, CaseIterable, Identifiable {
    case yellow
    case green
    case blue
    case pink
    case orange

    var id: String { rawValue }

    var color: Color {
        switch self {
        case .yellow: Color.yellow.opacity(0.33)
        case .green: Color.green.opacity(0.30)
        case .blue: Color.blue.opacity(0.30)
        case .pink: Color.pink.opacity(0.30)
        case .orange: Color.orange.opacity(0.33)
        }
    }

    var solidColor: Color {
        switch self {
        case .yellow: .yellow
        case .green: .green
        case .blue: .blue
        case .pink: .pink
        case .orange: .orange
        }
    }

    var displayName: String { rawValue.capitalized }
}
