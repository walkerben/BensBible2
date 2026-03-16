import Foundation

enum ExerciseType: String {
    case fillBlank
    case wordDrag
    case multipleChoice
}

enum FillBlankSegment {
    case word(String)
    case blank(answer: String)
}

enum ExerciseState {
    case fillBlank(verse: MemorizedVerse, segments: [FillBlankSegment])
    case wordDrag(verse: MemorizedVerse, shuffledWords: [String], correctOrder: [String])
    case multipleChoice(verse: MemorizedVerse, options: [String])

    var verse: MemorizedVerse {
        switch self {
        case .fillBlank(let v, _): return v
        case .wordDrag(let v, _, _): return v
        case .multipleChoice(let v, _): return v
        }
    }

    var exerciseType: ExerciseType {
        switch self {
        case .fillBlank: return .fillBlank
        case .wordDrag: return .wordDrag
        case .multipleChoice: return .multipleChoice
        }
    }
}
