import SwiftUI
import SwiftData

struct ReadingDayView: View {
    let plan: ReadingPlan
    let day: ReadingPlanDay
    let viewModel: ReadingPlanViewModel
    let bibleDataService: any BibleDataService

    @State private var isComplete: Bool = false
    @State private var chapterSections: [(bookName: String, chapter: Int, verses: [Verse])] = []
    @State private var isLoading = true
    @State private var annotations: [String: VerseAnnotation] = [:]

    // Verse selection
    @State private var selectedVerseIDs: Set<VerseID> = []

    // Sheet state
    @State private var isHighlightPickerPresented = false
    @State private var isNoteEditorPresented = false
    @State private var noteEditingVerseID: VerseID?
    @State private var noteEditingText = ""
    @State private var isAddToPresentationPresented = false
    @State private var isAddToMemorizePresented = false
    @State private var isSharePresented = false

    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext

    private var annotationService: SwiftDataAnnotationService {
        SwiftDataAnnotationService(modelContext: modelContext)
    }

    // Derives the selected verse tuples needed by presentation/memorize/share sheets
    private var selectedVerseTuples: [(bookName: String, chapterNumber: Int, verseNumber: Int, text: String)] {
        selectedVerseIDs.sorted { ($0.chapter, $0.verse) < ($1.chapter, $1.verse) }.compactMap { id in
            guard let section = chapterSections.first(where: { $0.bookName == id.book && $0.chapter == id.chapter }),
                  let verse = section.verses.first(where: { $0.number == id.verse })
            else { return nil }
            return (bookName: id.book, chapterNumber: id.chapter, verseNumber: id.verse, text: verse.text)
        }
    }

    private var selectedVerseTextsForShare: [(number: Int, text: String)] {
        selectedVerseTuples.map { (number: $0.verseNumber, text: $0.text) }
    }

    private var selectedVerseReference: String {
        VerseID.displayRange(from: selectedVerseIDs)
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 0) {
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 0) {
                        if isLoading {
                            ProgressView()
                                .frame(maxWidth: .infinity, alignment: .center)
                                .padding(.top, 40)
                        } else {
                            ForEach(chapterSections, id: \.chapter) { section in
                                Text("\(section.bookName) \(section.chapter)")
                                    .font(.title3)
                                    .fontWeight(.bold)
                                    .padding(.horizontal)
                                    .padding(.top, 20)
                                    .padding(.bottom, 8)

                                ForEach(section.verses) { verse in
                                    let verseID = VerseID(book: section.bookName, chapter: section.chapter, verse: verse.number)
                                    VerseRow(
                                        verse: verse,
                                        isSelected: selectedVerseIDs.contains(verseID),
                                        annotation: annotations[verseID.key],
                                        onTap: {
                                            if selectedVerseIDs.contains(verseID) {
                                                selectedVerseIDs.remove(verseID)
                                            } else {
                                                selectedVerseIDs.insert(verseID)
                                            }
                                        },
                                        onNoteTap: {
                                            noteEditingVerseID = verseID
                                            noteEditingText = annotations[verseID.key]?.noteText ?? ""
                                            isNoteEditorPresented = true
                                        }
                                    )
                                }
                            }
                            Spacer(minLength: 20)
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                    .padding(.bottom, selectedVerseIDs.isEmpty ? 0 : 70)
                }

                // Mark Complete — hidden while verses are selected
                if selectedVerseIDs.isEmpty {
                    VStack(spacing: 0) {
                        Divider()
                        Button {
                            guard !isComplete else { return }
                            withAnimation(.easeInOut(duration: 0.3)) {
                                isComplete = true
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
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                }
            }

            // Verse action bar — slides up over the Mark Complete button
            if !selectedVerseIDs.isEmpty {
                VerseActionBar(
                    selectedCount: selectedVerseIDs.count,
                    onHighlight: { isHighlightPickerPresented = true },
                    onNote: {
                        if let first = selectedVerseIDs.sorted(by: { ($0.chapter, $0.verse) < ($1.chapter, $1.verse) }).first {
                            noteEditingVerseID = first
                            noteEditingText = annotations[first.key]?.noteText ?? ""
                            isNoteEditorPresented = true
                        }
                    },
                    onBookmark: {
                        annotationService.toggleBookmark(for: Array(selectedVerseIDs))
                        loadAnnotations()
                        selectedVerseIDs = []
                    },
                    onAddToPresentation: { isAddToPresentationPresented = true },
                    onAddToMemorize: { isAddToMemorizePresented = true },
                    onCopy: {
                        let entries = selectedVerseTuples
                        let lines = entries.count == 1
                            ? entries[0].text
                            : entries.map { "\($0.verseNumber) \($0.text)" }.joined(separator: "\n")
                        UIPasteboard.general.string = "\(lines)\n\n— \(selectedVerseReference)"
                        selectedVerseIDs = []
                    },
                    onShare: { isSharePresented = true },
                    onDeselectAll: { selectedVerseIDs = [] }
                )
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
        }
        .animation(.spring(duration: 0.3), value: selectedVerseIDs.isEmpty)
        .navigationTitle("Day \(day.dayNumber)")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            isComplete = viewModel.isDayComplete(planId: plan.id, dayNumber: day.dayNumber)
            loadChapters()
        }
        .sheet(isPresented: $isHighlightPickerPresented) {
            HighlightColorPicker { color in
                annotationService.setHighlight(color: color, for: Array(selectedVerseIDs))
                loadAnnotations()
                selectedVerseIDs = []
                isHighlightPickerPresented = false
            }
            .presentationDetents([.medium])
        }
        .sheet(isPresented: $isNoteEditorPresented) {
            NoteEditorView(
                verseReference: noteEditingVerseID?.displayReference ?? "",
                text: $noteEditingText,
                onSave: {
                    if let id = noteEditingVerseID {
                        annotationService.saveNote(text: noteEditingText, for: id)
                        loadAnnotations()
                    }
                    noteEditingVerseID = nil
                    noteEditingText = ""
                    selectedVerseIDs = []
                    isNoteEditorPresented = false
                },
                onCancel: {
                    noteEditingVerseID = nil
                    noteEditingText = ""
                    isNoteEditorPresented = false
                }
            )
        }
        .sheet(isPresented: $isAddToPresentationPresented) {
            AddToPresentationSheet(
                verseTexts: selectedVerseTuples,
                onDone: {
                    isAddToPresentationPresented = false
                    selectedVerseIDs = []
                }
            )
        }
        .sheet(isPresented: $isAddToMemorizePresented) {
            AddToMemorizeSheet(
                verses: selectedVerseTuples,
                onDone: {
                    isAddToMemorizePresented = false
                    selectedVerseIDs = []
                }
            )
        }
        .sheet(isPresented: $isSharePresented) {
            VerseShareSheet(
                verses: selectedVerseTextsForShare,
                reference: selectedVerseReference
            )
        }
    }

    private func loadChapters() {
        isLoading = true
        Task {
            var sections: [(bookName: String, chapter: Int, verses: [Verse])] = []
            for entry in day.readings {
                if let chapter = try? bibleDataService.loadChapter(bookName: entry.bookName, chapter: entry.chapter) {
                    sections.append((bookName: entry.bookName, chapter: entry.chapter, verses: chapter.verses))
                }
            }
            await MainActor.run {
                chapterSections = sections
                isLoading = false
                loadAnnotations()
            }
        }
    }

    private func loadAnnotations() {
        var all: [String: VerseAnnotation] = [:]
        for section in chapterSections {
            let chapterAnnotations = annotationService.fetchAnnotations(book: section.bookName, chapter: section.chapter)
            all.merge(chapterAnnotations) { _, new in new }
        }
        annotations = all
    }
}
