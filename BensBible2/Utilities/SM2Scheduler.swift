import Foundation

struct SM2Result {
    let repetitions: Int
    let easeFactor: Double
    let intervalDays: Int
    let nextReviewDate: Date
}

struct SM2Scheduler {
    static func process(quality: Int, repetitions: Int, easeFactor: Double, intervalDays: Int) -> SM2Result {
        var newRepetitions = repetitions
        var newEaseFactor = easeFactor
        var newInterval = intervalDays

        if quality >= 3 {
            newRepetitions += 1
            switch newRepetitions {
            case 1:
                newInterval = 1
            case 2:
                newInterval = 6
            default:
                newInterval = Int((Double(intervalDays) * newEaseFactor).rounded())
            }
            let delta = 0.1 - Double(5 - quality) * (0.08 + Double(5 - quality) * 0.02)
            newEaseFactor = max(1.3, newEaseFactor + delta)
        } else {
            newRepetitions = 0
            newInterval = 1
        }

        let today = Calendar.current.startOfDay(for: Date())
        let nextDate = Calendar.current.date(byAdding: .day, value: newInterval, to: today) ?? Date()
        return SM2Result(
            repetitions: newRepetitions,
            easeFactor: newEaseFactor,
            intervalDays: newInterval,
            nextReviewDate: nextDate
        )
    }
}
