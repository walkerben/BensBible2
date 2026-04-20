import SwiftUI

struct ReadingPlanDetailView: View {
    let plan: ReadingPlan
    let viewModel: ReadingPlanViewModel
    let bibleDataService: any BibleDataService

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let progress = viewModel.progress(for: plan)

        List {
            // Completed banner
            if progress?.isCompleted == true {
                Section {
                    HStack(spacing: 12) {
                        Image(systemName: "checkmark.seal.fill")
                            .font(.title)
                            .foregroundStyle(.yellow)
                        VStack(alignment: .leading) {
                            Text("Plan Complete!")
                                .font(.headline)
                                .foregroundStyle(.yellow)
                            Text("You finished \(plan.title)")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }

            // Header info
            Section {
                VStack(alignment: .leading, spacing: 8) {
                    Text(plan.title)
                        .font(.title2)
                        .fontWeight(.bold)
                    Text(plan.description)
                        .font(.body)
                        .foregroundStyle(.secondary)
                    Label(plan.category, systemImage: "tag")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                .padding(.vertical, 4)
            }

            // Start or Continue section
            if progress == nil {
                Section {
                    Button {
                        viewModel.startPlan(plan)
                    } label: {
                        Label("Start Plan", systemImage: "play.fill")
                            .frame(maxWidth: .infinity, alignment: .center)
                            .font(.headline)
                    }
                }
            } else if let currentDay = viewModel.currentDay(for: plan) {
                Section(header: Text("Continue Reading")) {
                    NavigationLink(destination: ReadingDayView(
                        plan: plan,
                        day: currentDay,
                        viewModel: viewModel,
                        bibleDataService: bibleDataService
                    )) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Day \(currentDay.dayNumber)")
                                .font(.headline)
                                .foregroundStyle(Color.accentColor)
                            Text(currentDay.referenceText)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 4)
                    }
                    .listRowBackground(Color.accentColor.opacity(0.08))
                }
            }

            // All days
            Section(header: Text("All Days")) {
                ForEach(plan.days, id: \.dayNumber) { day in
                    NavigationLink(destination: ReadingDayView(
                        plan: plan,
                        day: day,
                        viewModel: viewModel,
                        bibleDataService: bibleDataService
                    )) {
                        DayRowView(plan: plan, day: day, viewModel: viewModel)
                    }
                }
            }
        }
        .navigationTitle(plan.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Done") { dismiss() }
            }
        }
    }
}

private struct DayRowView: View {
    let plan: ReadingPlan
    let day: ReadingPlanDay
    let viewModel: ReadingPlanViewModel

    var body: some View {
        let complete = viewModel.isDayComplete(planId: plan.id, dayNumber: day.dayNumber)
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text("Day \(day.dayNumber)")
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundStyle(complete ? .secondary : .primary)
                Text(day.referenceText)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Spacer()
            if complete {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundStyle(.green)
            }
        }
        .padding(.vertical, 2)
    }
}
