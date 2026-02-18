import SwiftUI
import SwiftData

struct ReaderView: View {
    @State var viewModel = ReaderViewModel()
    @Environment(\.modelContext) private var modelContext
    @Environment(NavigationCoordinator.self) private var coordinator

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            if let chapter = viewModel.currentChapter {
                                ForEach(chapter.verses) { verse in
                                    VerseRow(
                                        verse: verse,
                                        isSelected: viewModel.isSelected(verse),
                                        annotation: viewModel.annotation(for: verse),
                                        onTap: { viewModel.toggleVerseSelection(verse) }
                                    )
                                }
                            }
                        }
                        .padding(.horizontal)
                        .padding(.vertical, 8)
                        .padding(.bottom, viewModel.hasSelection ? 70 : 0)
                    }
                    .onChange(of: viewModel.scrollToVerseID) { _, verseID in
                        if let verseID {
                            withAnimation {
                                proxy.scrollTo(verseID, anchor: .top)
                            }
                            viewModel.scrollToVerseID = nil
                        }
                    }
                    .onChange(of: viewModel.currentLocation) { _, _ in
                        proxy.scrollTo(viewModel.currentChapter?.verses.first?.verse, anchor: .top)
                    }
                }

                if viewModel.hasSelection {
                    VerseActionBar(
                        selectedCount: viewModel.selectedCount,
                        onHighlight: { viewModel.isHighlightPickerPresented = true },
                        onNote: { viewModel.beginNoteEditing() },
                        onBookmark: { viewModel.bookmarkSelectedVerses() },
                        onShare: { viewModel.isShareSheetPresented = true },
                        onDeselectAll: { viewModel.deselectAll() }
                    )
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .animation(.spring(duration: 0.3), value: viewModel.hasSelection)
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button {
                        viewModel.previousChapter()
                    } label: {
                        Image(systemName: "chevron.left")
                    }
                    .disabled(!viewModel.canGoPrevious)
                }

                ToolbarItem(placement: .principal) {
                    Button {
                        viewModel.isPickerPresented = true
                    } label: {
                        HStack(spacing: 4) {
                            Text(viewModel.currentLocation.displayTitle)
                                .font(.headline)
                            Image(systemName: "chevron.down")
                                .font(.caption)
                        }
                        .foregroundStyle(.primary)
                    }
                }

                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        viewModel.nextChapter()
                    } label: {
                        Image(systemName: "chevron.right")
                    }
                    .disabled(!viewModel.canGoNext)
                }
            }
            .sheet(isPresented: $viewModel.isPickerPresented) {
                BookChapterPickerView(viewModel: viewModel)
            }
            .sheet(isPresented: $viewModel.isHighlightPickerPresented) {
                HighlightColorPicker { color in
                    viewModel.applyHighlight(color)
                    viewModel.isHighlightPickerPresented = false
                }
            }
            .sheet(isPresented: $viewModel.isShareSheetPresented) {
                VerseShareSheet(
                    verses: viewModel.selectedVerseTexts,
                    reference: viewModel.selectedVerseReference
                )
            }
            .sheet(isPresented: $viewModel.isNoteEditorPresented) {
                NoteEditorView(
                    verseReference: viewModel.noteEditingVerseID?.displayReference ?? "",
                    text: $viewModel.noteEditingText,
                    onSave: {
                        viewModel.saveNote()
                        viewModel.isNoteEditorPresented = false
                    },
                    onCancel: {
                        viewModel.cancelNoteEditing()
                        viewModel.isNoteEditorPresented = false
                    }
                )
            }
        }
        .onAppear {
            viewModel.configure(annotationService: SwiftDataAnnotationService(modelContext: modelContext))
            viewModel.onAppear()
        }
        .onChange(of: coordinator.pendingNavigation) { _, location in
            if let location {
                viewModel.navigateTo(book: location.bookName, chapter: location.chapterNumber, verse: location.verseNumber)
                coordinator.pendingNavigation = nil
            }
        }
    }
}
