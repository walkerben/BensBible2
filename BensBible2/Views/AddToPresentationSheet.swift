import SwiftUI
import SwiftData

struct AddToPresentationSheet: View {
    let verseTexts: [(bookName: String, chapterNumber: Int, verseNumber: Int, text: String)]
    let onDone: () -> Void

    @Environment(\.modelContext) private var modelContext
    @Environment(\.dismiss) private var dismiss
    @Query(sort: \Presentation.createdAt, order: .reverse) private var presentations: [Presentation]
    @State private var showNewAlert = false
    @State private var newName = ""

    var body: some View {
        NavigationStack {
            List {
                Section {
                    Button {
                        newName = ""
                        showNewAlert = true
                    } label: {
                        Label("New Presentation…", systemImage: "plus.circle")
                    }
                }

                Section("Add to existing") {
                    if presentations.isEmpty {
                        Text("No presentations yet.")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(presentations) { presentation in
                            Button {
                                addSlides(to: presentation)
                            } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(presentation.name)
                                            .foregroundStyle(.primary)
                                        Text("\(presentation.slides.count) slide\(presentation.slides.count == 1 ? "" : "s")")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Add to Presentation")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
        .alert("New Presentation", isPresented: $showNewAlert) {
            TextField("Name", text: $newName)
            Button("Create & Add") {
                let trimmed = newName.trimmingCharacters(in: .whitespaces)
                guard !trimmed.isEmpty else { return }
                let presentation = Presentation(name: trimmed)
                modelContext.insert(presentation)
                addSlides(to: presentation)
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Enter a name for your new presentation.")
        }
    }

    private func addSlides(to presentation: Presentation) {
        let existingCount = presentation.slides.count
        for (i, entry) in verseTexts.enumerated() {
            let slide = PresentationSlide(
                bookName: entry.bookName,
                chapterNumber: entry.chapterNumber,
                verseNumber: entry.verseNumber,
                verseText: entry.text,
                order: existingCount + i
            )
            slide.presentation = presentation
            modelContext.insert(slide)
        }
        onDone()
        dismiss()
    }
}
