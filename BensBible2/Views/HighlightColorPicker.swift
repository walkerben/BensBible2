import SwiftUI

struct HighlightColorPicker: View {
    let onSelect: (HighlightColor?) -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("Highlight Color")
                .font(.headline)

            HStack(spacing: 16) {
                ForEach(HighlightColor.allCases) { color in
                    Button {
                        onSelect(color)
                    } label: {
                        Circle()
                            .fill(color.solidColor)
                            .frame(width: 40, height: 40)
                            .overlay(
                                Circle().stroke(Color.primary.opacity(0.2), lineWidth: 1)
                            )
                    }
                }

                Button {
                    onSelect(nil)
                } label: {
                    Circle()
                        .fill(Color.clear)
                        .frame(width: 40, height: 40)
                        .overlay(
                            Circle().stroke(Color.primary.opacity(0.3), lineWidth: 1)
                        )
                        .overlay(
                            Image(systemName: "xmark")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        )
                }
            }
        }
        .padding()
        .presentationDetents([.height(120)])
    }
}
