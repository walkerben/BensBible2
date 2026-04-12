import Foundation
import SwiftData
import Observation

@Observable final class ReadingPlanViewModel {
    private let modelContext: ModelContext
    var progressMap: [String: ReadingPlanProgress] = [:]

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
        load()
    }

    func load() {
        let items = (try? modelContext.fetch(FetchDescriptor<ReadingPlanProgress>())) ?? []
        progressMap = Dictionary(uniqueKeysWithValues: items.map { ($0.planId, $0) })
    }

    func progress(for plan: ReadingPlan) -> ReadingPlanProgress? {
        progressMap[plan.id]
    }

    func startPlan(_ plan: ReadingPlan) {
        guard progressMap[plan.id] == nil else { return }
        let progress = ReadingPlanProgress(planId: plan.id)
        modelContext.insert(progress)
        progressMap[plan.id] = progress
    }

    func markDayComplete(planId: String, dayNumber: Int, totalDays: Int) {
        guard let progress = progressMap[planId] else { return }
        var days = progress.completedDays
        days.insert(dayNumber)
        progress.completedDays = days
        if days.count == totalDays {
            progress.completedAt = Date()
        }
        try? modelContext.save()
    }

    func isDayComplete(planId: String, dayNumber: Int) -> Bool {
        progressMap[planId]?.completedDays.contains(dayNumber) ?? false
    }

    func currentDay(for plan: ReadingPlan) -> ReadingPlanDay? {
        guard let progress = progressMap[plan.id] else { return plan.days.first }
        let completed = progress.completedDays
        return plan.days.first(where: { !completed.contains($0.dayNumber) })
    }
}
