import SwiftUI
import SwiftData

struct PresentationsView: View {
    @Environment(\.modelContext) private var modelContext
    @Query(sort: \Presentation.createdAt, order: .reverse) private var presentations: [Presentation]
    @State private var showNewAlert = false
    @State private var newName = ""

    var body: some View {
        NavigationStack {
            List {
                ForEach(presentations) { presentation in
                    NavigationLink(destination: PresentationDetailView(presentation: presentation)) {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(presentation.name)
                                .font(.headline)
                            Text("\(presentation.slides.count) slide\(presentation.slides.count == 1 ? "" : "s")")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        .padding(.vertical, 4)
                    }
                    .contextMenu {
                        Button(role: .destructive) {
                            modelContext.delete(presentation)
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
                }
                .onDelete { indexSet in
                    for index in indexSet {
                        modelContext.delete(presentations[index])
                    }
                }
            }
            .navigationTitle("Presentations")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        newName = ""
                        showNewAlert = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .overlay {
                if presentations.isEmpty {
                    ContentUnavailableView(
                        "No Presentations",
                        systemImage: "play.rectangle",
                        description: Text("Tap + to create a presentation.")
                    )
                }
            }
            .onAppear {
                let viewModel = PresentationsViewModel(modelContext: modelContext)
                viewModel.seedRomanRoadIfNeeded()
            }
        }
        .alert("New Presentation", isPresented: $showNewAlert) {
            TextField("Name", text: $newName)
            Button("Create") {
                let trimmed = newName.trimmingCharacters(in: .whitespaces)
                if !trimmed.isEmpty {
                    let p = Presentation(name: trimmed)
                    modelContext.insert(p)
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Enter a name for your new presentation.")
        }
    }
}
