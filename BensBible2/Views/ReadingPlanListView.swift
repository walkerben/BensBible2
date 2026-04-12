import SwiftUI
import SwiftData

struct ReadingPlanListView: View {
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: ReadingPlanViewModel?

    private let bibleDataService: any BibleDataService

    init(bibleDataService: any BibleDataService) {
        self.bibleDataService = bibleDataService
    }

    var body: some View {
        NavigationStack {
            Group {
                if let vm = viewModel {
                    planList(viewModel: vm)
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Reading Plans")
        }
        .onAppear {
            if viewModel == nil {
                viewModel = ReadingPlanViewModel(modelContext: modelContext)
            }
        }
    }

    @ViewBuilder
    private func planList(viewModel: ReadingPlanViewModel) -> some View {
        let categories = ["Bible in a Year", "New Testament", "Old Testament"]
        List {
            ForEach(categories, id: \.self) { category in
                Section(header: Text(category)) {
                    ForEach(allReadingPlans.filter { $0.category == category }, id: \.id) { plan in
                        NavigationLink(destination: ReadingPlanDetailView(
                            plan: plan,
                            viewModel: viewModel,
                            bibleDataService: bibleDataService
                        )) {
                            PlanRowView(plan: plan, viewModel: viewModel)
                        }
                    }
                }
            }
        }
    }
}

private struct PlanRowView: View {
    let plan: ReadingPlan
    let viewModel: ReadingPlanViewModel

    var body: some View {
        let progress = viewModel.progress(for: plan)
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                if progress?.isCompleted == true {
                    Image(systemName: "checkmark.seal.fill")
                        .foregroundStyle(.yellow)
                }
                Text(plan.title)
                    .font(.headline)
            }

            if let progress, !progress.isCompleted {
                Text("\(plan.category) · \(progress.completedCount) / \(plan.totalDays) days")
                    .font(.caption)
                    .foregroundStyle(.secondary)
                ProgressView(value: Double(progress.completedCount), total: Double(plan.totalDays))
                    .tint(.accentColor)
            } else {
                Text(plan.category)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.vertical, 2)
    }
}
