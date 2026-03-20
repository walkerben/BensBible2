import SwiftUI

struct VerseActionBar: View {
    let selectedCount: Int
    let onHighlight: () -> Void
    let onNote: () -> Void
    let onBookmark: () -> Void
    let onAddToPresentation: () -> Void
    let onAddToMemorize: () -> Void
    let onCopy: () -> Void
    let onShare: () -> Void
    let onDeselectAll: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            HStack(spacing: 4) {
                Button(action: onHighlight) {
                    Label("Highlight", systemImage: "highlighter")
                        .labelStyle(.iconOnly)
                }

                Button(action: onNote) {
                    Label("Note", systemImage: "note.text.badge.plus")
                        .labelStyle(.iconOnly)
                }

                Button(action: onBookmark) {
                    Label("Bookmark", systemImage: "bookmark")
                        .labelStyle(.iconOnly)
                }

                Button(action: onAddToPresentation) {
                    Label("Present", systemImage: "play.rectangle")
                        .labelStyle(.iconOnly)
                }

                Button(action: onAddToMemorize) {
                    Label("Memorize", systemImage: "brain")
                        .labelStyle(.iconOnly)
                }

                Button(action: onCopy) {
                    Label("Copy", systemImage: "doc.on.doc")
                        .labelStyle(.iconOnly)
                }

                Button(action: onShare) {
                    Label("Share", systemImage: "square.and.arrow.up")
                        .labelStyle(.iconOnly)
                }
            }
            .font(.system(size: 20))

            Spacer()

            Text("\(selectedCount) selected")
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(1)

            Button(action: onDeselectAll) {
                Label("Deselect", systemImage: "xmark.circle.fill")
                    .labelStyle(.iconOnly)
                    .foregroundStyle(.secondary)
            }
            .font(.system(size: 20))
            .padding(.leading, 8)
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .background(.ultraThinMaterial)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .shadow(color: .black.opacity(0.15), radius: 8, y: 4)
        .padding(.horizontal, 16)
        .padding(.bottom, 8)
    }
}
