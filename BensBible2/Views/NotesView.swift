import SwiftUI
import SwiftData

struct NotesView: View {
    @State private var viewModel = NotesViewModel()
    @Environment(NavigationCoordinator.self) private var coordinator
    @Environment(\.modelContext) private var modelContext

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.notes.isEmpty {
                    ContentUnavailableView(
                        "No Notes",
                        systemImage: "note.text",
                        description: Text("Your verse notes will appear here.")
                    )
                } else {
                    List {
                        ForEach(viewModel.notes, id: \.verseKey) { annotation in
                            VStack(alignment: .leading, spacing: 4) {
                                HStack {
                                    Button {
                                        coordinator.navigateToReader(
                                            location: BibleLocation(
                                                bookName: annotation.bookName,
                                                chapterNumber: annotation.chapterNumber,
                                                verseNumber: annotation.verseNumber
                                            )
                                        )
                                    } label: {
                                        Text("\(annotation.bookName) \(annotation.chapterNumber):\(annotation.verseNumber)")
                                            .font(.headline)
                                            .foregroundStyle(.primary)
                                    }

                                    Spacer()

                                    Button {
                                        viewModel.beginEditing(annotation)
                                    } label: {
                                        Image(systemName: "pencil")
                                            .foregroundStyle(.secondary)
                                    }
                                    .buttonStyle(.borderless)
                                }

                                Text(annotation.noteText ?? "")
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                                    .lineLimit(2)
                            }
                            .swipeActions(edge: .trailing) {
                                Button(role: .destructive) {
                                    viewModel.deleteNote(annotation)
                                } label: {
                                    Label("Delete", systemImage: "trash")
                                }
                            }
                            .contextMenu {
                                Button {
                                    viewModel.beginEditing(annotation)
                                } label: {
                                    Label("Edit Note", systemImage: "pencil")
                                }
                                Button(role: .destructive) {
                                    viewModel.deleteNote(annotation)
                                } label: {
                                    Label("Delete Note", systemImage: "trash")
                                }
                            }
                        }
                    }
                }
            }
            .navigationTitle("Notes")
            .sheet(isPresented: Binding(
                get: { viewModel.isEditorPresented },
                set: { viewModel.isEditorPresented = $0 }
            )) {
                NoteEditorView(
                    verseReference: {
                        if let a = viewModel.editingAnnotation {
                            return "\(a.bookName) \(a.chapterNumber):\(a.verseNumber)"
                        }
                        return ""
                    }(),
                    text: Binding(
                        get: { viewModel.editingText },
                        set: { viewModel.editingText = $0 }
                    ),
                    onSave: {
                        viewModel.saveEdit()
                        viewModel.isEditorPresented = false
                    },
                    onCancel: {
                        viewModel.cancelEdit()
                        viewModel.isEditorPresented = false
                    }
                )
            }
        }
        .onAppear {
            viewModel.configure(annotationService: SwiftDataAnnotationService(modelContext: modelContext))
            viewModel.load()
        }
    }
}
