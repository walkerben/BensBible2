import SwiftUI

struct VerseShareImageView: View {
    let verses: [(number: Int, text: String)]
    let reference: String
    let gradient: ShareGradient
    let shareFont: ShareFont
    let fontSize: CGFloat

    var body: some View {
        ZStack {
            LinearGradient(
                colors: gradient.colors,
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )

            VStack(spacing: 24) {
                Spacer()

                Text("\u{201C}")
                    .font(.system(size: 72, weight: .bold, design: .serif))
                    .foregroundStyle(gradient.accentColor.opacity(0.5))

                Text(verseText)
                    .font(shareFont.font(size: fontSize))
                    .foregroundStyle(gradient.textColor)
                    .multilineTextAlignment(.center)
                    .lineSpacing(fontSize * 0.3)
                    .minimumScaleFactor(0.5)
                    .padding(.horizontal, 60)

                Rectangle()
                    .fill(gradient.accentColor.opacity(0.4))
                    .frame(width: 60, height: 2)

                Text(reference)
                    .font(shareFont.italicFont(size: fontSize * 0.7))
                    .foregroundStyle(gradient.accentColor)

                Spacer()
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .frame(width: 1080, height: 1080)
    }

    private var verseText: String {
        if verses.count == 1 {
            return verses[0].text
        }
        return verses.map { "\($0.number) \($0.text)" }.joined(separator: " ")
    }
}
