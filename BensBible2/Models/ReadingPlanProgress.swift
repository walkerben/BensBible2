import Foundation
import SwiftData

@Model final class ReadingPlanProgress {
    var id: UUID = UUID()
    var planId: String = ""
    var startedAt: Date = Date()
    var completedAt: Date? = nil
    var completedDaysData: String = ""

    init(planId: String) {
        self.planId = planId
    }

    var completedDays: Set<Int> {
        get {
            Set(completedDaysData.split(separator: ",").compactMap { Int($0) })
        }
        set {
            completedDaysData = newValue.sorted().map(String.init).joined(separator: ",")
        }
    }

    var isCompleted: Bool { completedAt != nil }
    var completedCount: Int { completedDays.count }
}
