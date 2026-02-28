import SwiftUI
import SwiftData

struct PresentationDetailView: View {
    @Environment(\.modelContext) private var modelContext
    @Bindable var presentation: Presentation
    @State private var isPresentingSlideshow = false

    var sortedSlides: [PresentationSlide] {
        presentation.slides.sorted { $0.order < $1.order }
    }

    var body: some View {
        List {
            ForEach(sortedSlides) { slide in
                VStack(alignment: .leading, spacing: 4) {
                    Text(slide.reference)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text(slide.verseText)
                        .font(.body)
                        .lineLimit(2)
                }
                .padding(.vertical, 4)
                .contextMenu {
                    Button(role: .destructive) {
                        modelContext.delete(slide)
                    } label: {
                        Label("Delete", systemImage: "trash")
                    }
                }
            }
            .onMove { from, to in
                var reordered = sortedSlides
                reordered.move(fromOffsets: from, toOffset: to)
                for (index, slide) in reordered.enumerated() {
                    slide.order = index
                }
            }
            .onDelete { indexSet in
                for index in indexSet {
                    modelContext.delete(sortedSlides[index])
                }
            }
        }
        .navigationTitle(presentation.name)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button {
                    isPresentingSlideshow = true
                } label: {
                    Label("Present", systemImage: "play.fill")
                }
                .disabled(presentation.slides.isEmpty)
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                EditButton()
            }
        }
        .fullScreenCover(isPresented: $isPresentingSlideshow) {
            PresentationSlideshowView(slides: sortedSlides)
        }
        .overlay {
            if presentation.slides.isEmpty {
                ContentUnavailableView(
                    "No Slides",
                    systemImage: "play.rectangle",
                    description: Text("Select verses in the Reader and add them here.")
                )
            }
        }
    }
}
