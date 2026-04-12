import SwiftUI

struct ReadingDayView: View {
    let plan: ReadingPlan
    let day: ReadingPlanDay
    let viewModel: ReadingPlanViewModel
    let bibleDataService: any BibleDataService

    @State private var isComplete: Bool = false
    @State private var chapterSections: [(title: String, verses: [(number: Int, text: String)])] = []
    @State private var isLoading = true
    @State private var markedComplete = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0, pinnedViews: []) {
                    if isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity, alignment: .center)
                            .padding(.top, 40)
                    } else {
                        ForEach(chapterSections, id: \.title) { section in
                            Text(section.title)
                                .font(.title3)
                                .fontWeight(.bold)
                                .padding(.horizontal)
                                .padding(.top, 20)
                                .padding(.bottom, 8)

                            ForEach(section.verses, id: \.number) { verse in
                                HStack(alignment: .top, spacing: 8) {
                                    Text("\(verse.number)")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                        .frame(minWidth: 24, alignment: .trailing)
                                        .padding(.top, 2)
                                    Text(verse.text)
                                        .font(.body)
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                                .padding(.horizontal)
                                .padding(.vertical, 3)
                            }
                        }
                        Spacer(minLength: 20)
                    }
                }
            }

            // Mark Complete button
            VStack(spacing: 0) {
                Divider()
                Button {
                    guard !isComplete else { return }
                    withAnimation(.easeInOut(duration: 0.3)) {
                        isComplete = true
                        markedComplete = true
                    }
                    viewModel.markDayComplete(
                        planId: plan.id,
                        dayNumber: day.dayNumber,
                        totalDays: plan.totalDays
                    )
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                        dismiss()
                    }
                } label: {
                    HStack {
                        Image(systemName: isComplete ? "checkmark.circle.fill" : "checkmark.circle")
                        Text(isComplete ? "Completed" : "Mark as Complete")
                    }
                    .font(.headline)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(isComplete ? Color.green.opacity(0.15) : Color.accentColor)
                    .foregroundStyle(isComplete ? .green : .white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .padding(.horizontal)
                    .padding(.vertical, 12)
                }
                .disabled(isComplete)
            }
            .background(.regularMaterial)
        }
        .navigationTitle("Day \(day.dayNumber)")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            isComplete = viewModel.isDayComplete(planId: plan.id, dayNumber: day.dayNumber)
            loadChapters()
        }
    }

    private func loadChapters() {
        isLoading = true
        Task {
            var sections: [(title: String, verses: [(number: Int, text: String)])] = []
            for entry in day.readings {
                let title = "\(entry.bookName) \(entry.chapter)"
                if let chapter = try? bibleDataService.loadChapter(bookName: entry.bookName, chapter: entry.chapter) {
                    let verses = chapter.verses.map { (number: $0.number, text: $0.text) }
                    sections.append((title: title, verses: verses))
                }
            }
            await MainActor.run {
                chapterSections = sections
                isLoading = false
            }
        }
    }
}
