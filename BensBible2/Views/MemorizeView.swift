import SwiftUI
import SwiftData

struct MemorizeView: View {
    @State private var viewModel = MemorizeViewModel()
    @Environment(\.modelContext) private var modelContext
    @State private var isSessionPresented = false

    @ViewBuilder
    private var memorizeContent: some View {
        if viewModel.memorizedVerses.isEmpty {
            ContentUnavailableView(
                "No Verses",
                systemImage: "brain",
                description: Text("Add verses from the Reader to start memorizing.")
            )
        } else {
            List {
                Section {
                    HStack(spacing: 16) {
                        Label("\(viewModel.memorizedVerses.count) verses", systemImage: "book.closed")
                        Spacer()
                        Label("\(viewModel.dueVerses.count) due today", systemImage: "clock")
                            .foregroundStyle(viewModel.dueVerses.isEmpty ? Color.secondary : Color.orange)
                    }
                    .font(.subheadline)
                }

                Section("Verses") {
                    ForEach(viewModel.memorizedVerses) { verse in
                        MemorizedVerseRow(verse: verse)
                    }
                    .onDelete { indexSet in
                        indexSet.forEach { i in
                            viewModel.removeVerse(viewModel.memorizedVerses[i])
                        }
                    }
                }
            }
        }
    }

    var body: some View {
        NavigationStack {
            memorizeContent
            .navigationTitle("Memorize")
            .toolbar {
                if !viewModel.dueVerses.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button("Practice") {
                            viewModel.startSession()
                            isSessionPresented = true
                        }
                        .buttonStyle(.borderedProminent)
                    }
                }
            }
            .fullScreenCover(isPresented: $isSessionPresented) {
                MemorizeSessionView(viewModel: viewModel) {
                    isSessionPresented = false
                }
            }
        }
        .onAppear {
            viewModel.configure(modelContext: modelContext)
            viewModel.seedDefaultVersesIfNeeded()
        }
    }
}

struct MemorizedVerseRow: View {
    let verse: MemorizedVerse

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(verse.reference)
                    .font(.headline)
                Spacer()
                intervalBadge
            }
            Text(verse.verseText)
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(2)
        }
        .padding(.vertical, 4)
    }

    @ViewBuilder
    private var intervalBadge: some View {
        if verse.isDue {
            Text("Due")
                .font(.caption2.bold())
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(.orange.opacity(0.2))
                .foregroundStyle(.orange)
                .clipShape(Capsule())
        } else {
            Text("In \(verse.intervalDays)d")
                .font(.caption2.bold())
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(.green.opacity(0.2))
                .foregroundStyle(.green)
                .clipShape(Capsule())
        }
    }
}
