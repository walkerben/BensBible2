import SwiftUI

enum ShareGradient: String, CaseIterable, Identifiable {
    case navy
    case sunset
    case forest
    case parchment
    case midnight

    var id: String { rawValue }

    var colors: [Color] {
        switch self {
        case .navy: [Color(red: 0.05, green: 0.1, blue: 0.3), Color(red: 0.15, green: 0.25, blue: 0.55)]
        case .sunset: [Color(red: 0.95, green: 0.5, blue: 0.2), Color(red: 0.85, green: 0.25, blue: 0.4)]
        case .forest: [Color(red: 0.05, green: 0.2, blue: 0.15), Color(red: 0.1, green: 0.4, blue: 0.35)]
        case .parchment: [Color(red: 0.96, green: 0.93, blue: 0.85), Color(red: 0.88, green: 0.82, blue: 0.7)]
        case .midnight: [Color(red: 0.05, green: 0.02, blue: 0.1), Color(red: 0.2, green: 0.08, blue: 0.35)]
        }
    }

    var textColor: Color {
        switch self {
        case .parchment: Color(red: 0.2, green: 0.15, blue: 0.1)
        default: .white
        }
    }

    var accentColor: Color {
        switch self {
        case .navy: Color(red: 0.6, green: 0.75, blue: 1.0)
        case .sunset: Color(red: 1.0, green: 0.9, blue: 0.7)
        case .forest: Color(red: 0.5, green: 0.85, blue: 0.7)
        case .parchment: Color(red: 0.55, green: 0.4, blue: 0.25)
        case .midnight: Color(red: 0.7, green: 0.55, blue: 0.95)
        }
    }
}
