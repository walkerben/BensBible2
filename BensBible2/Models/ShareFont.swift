import SwiftUI

enum ShareFont: String, CaseIterable, Identifiable {
    case georgia = "Georgia"
    case palatino = "Palatino"
    case baskerville = "Baskerville"
    case helvetica = "Helvetica Neue"
    case avenir = "Avenir"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .georgia: "Georgia"
        case .palatino: "Palatino"
        case .baskerville: "Baskerville"
        case .helvetica: "Helvetica"
        case .avenir: "Avenir"
        }
    }

    func font(size: CGFloat) -> Font {
        .custom(rawValue, size: size)
    }

    func italicFont(size: CGFloat) -> Font {
        switch self {
        case .georgia: .custom("Georgia-Italic", size: size)
        case .palatino: .custom("Palatino-Italic", size: size)
        case .baskerville: .custom("Baskerville-Italic", size: size)
        case .helvetica: .custom("HelveticaNeue-Italic", size: size)
        case .avenir: .custom("Avenir-LightOblique", size: size)
        }
    }
}
