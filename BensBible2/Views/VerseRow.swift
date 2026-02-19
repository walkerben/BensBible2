import SwiftUI

struct VerseRow: View {
    let verse: Verse
    var isSelected: Bool = false
    var annotation: VerseAnnotation?
    var isHighlighted: Bool = false
    var onTap: (() -> Void)?

    var body: some View {
        HStack(alignment: .top, spacing: 0) {
            Text(attributedVerse)
                .font(.custom("Georgia", size: 18))
                .lineSpacing(6)
                .frame(maxWidth: .infinity, alignment: .leading)

            if let annotation, annotation.isBookmarked {
                Image(systemName: "bookmark.fill")
                    .font(.caption)
                    .foregroundStyle(.blue)
                    .padding(.top, 4)
            }

            if let annotation, annotation.noteText != nil {
                Image(systemName: "note.text")
                    .font(.caption)
                    .foregroundStyle(.orange)
                    .padding(.top, 4)
                    .padding(.leading, 4)
            }
        }
        .padding(.vertical, 2)
        .padding(.horizontal, 4)
        .background(backgroundColor)
        .animation(.easeInOut(duration: 0.5), value: isHighlighted)
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .contentShape(Rectangle())
        .onTapGesture {
            onTap?()
        }
        .id(verse.verse)
    }

    private var backgroundColor: Color {
        if isHighlighted {
            return Color.yellow.opacity(0.4)
        }
        if isSelected {
            return Color.blue.opacity(0.15)
        }
        if let color = annotation?.highlightColor {
            return color.color
        }
        return .clear
    }

    private var attributedVerse: AttributedString {
        var number = AttributedString("\(verse.verse) ")
        number.font = .custom("Georgia", size: 12)
        number.baselineOffset = 6
        number.foregroundColor = .secondary

        var text = AttributedString(verse.text)
        text.font = .custom("Georgia", size: 18)

        return number + text
    }
}
