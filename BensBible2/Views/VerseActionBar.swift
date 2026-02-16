import SwiftUI

struct VerseActionBar: View {
    let selectedCount: Int
    let onHighlight: () -> Void
    let onNote: () -> Void
    let onBookmark: () -> Void
    let onDeselectAll: () -> Void

    var body: some View {
        HStack(spacing: 20) {
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

            Spacer()

            Text("\(selectedCount) selected")
                .font(.caption)
                .foregroundStyle(.secondary)

            Button(action: onDeselectAll) {
                Label("Deselect", systemImage: "xmark.circle.fill")
                    .labelStyle(.iconOnly)
                    .foregroundStyle(.secondary)
            }
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
