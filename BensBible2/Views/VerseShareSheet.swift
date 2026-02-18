import SwiftUI

struct VerseShareSheet: View {
    let verses: [(number: Int, text: String)]
    let reference: String
    @State private var selectedGradient: ShareGradient = .navy
    @State private var selectedFont: ShareFont = .georgia
    @State private var fontSize: CGFloat = 28

    private let fontSizeRange: ClosedRange<CGFloat> = 24...56

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    VerseShareImageView(
                        verses: verses,
                        reference: reference,
                        gradient: selectedGradient,
                        shareFont: selectedFont,
                        fontSize: fontSize
                    )
                    .frame(width: 270, height: 270)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(color: .black.opacity(0.2), radius: 8, y: 4)
                    .padding(.top, 8)

                    // Gradient picker
                    HStack(spacing: 16) {
                        ForEach(ShareGradient.allCases) { gradient in
                            Button {
                                selectedGradient = gradient
                            } label: {
                                Circle()
                                    .fill(
                                        LinearGradient(
                                            colors: gradient.colors,
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                                    .frame(width: 40, height: 40)
                                    .overlay(
                                        Circle()
                                            .stroke(Color.primary, lineWidth: selectedGradient == gradient ? 2.5 : 0)
                                            .padding(selectedGradient == gradient ? -3 : 0)
                                    )
                            }
                        }
                    }

                    // Font picker
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Font")
                            .font(.caption)
                            .foregroundStyle(.secondary)

                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 10) {
                                ForEach(ShareFont.allCases) { font in
                                    Button {
                                        selectedFont = font
                                    } label: {
                                        Text(font.displayName)
                                            .font(.custom(font.rawValue, size: 14))
                                            .padding(.horizontal, 12)
                                            .padding(.vertical, 8)
                                            .background(
                                                RoundedRectangle(cornerRadius: 8)
                                                    .fill(selectedFont == font ? Color.accentColor : Color(.systemGray5))
                                            )
                                            .foregroundStyle(selectedFont == font ? .white : .primary)
                                    }
                                }
                            }
                        }
                    }
                    .padding(.horizontal, 20)

                    // Size slider
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Size")
                            .font(.caption)
                            .foregroundStyle(.secondary)

                        HStack(spacing: 12) {
                            Image(systemName: "textformat.size.smaller")
                                .foregroundStyle(.secondary)
                            Slider(value: $fontSize, in: fontSizeRange, step: 2)
                            Image(systemName: "textformat.size.larger")
                                .foregroundStyle(.secondary)
                        }
                    }
                    .padding(.horizontal, 20)

                    Button {
                        shareImage()
                    } label: {
                        Label("Share", systemImage: "square.and.arrow.up")
                            .font(.headline)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                    }
                    .buttonStyle(.borderedProminent)
                    .padding(.horizontal, 40)
                    .padding(.bottom, 20)
                }
            }
            .navigationTitle("Share Verse")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.large])
    }

    @Environment(\.dismiss) private var dismiss

    private func shareImage() {
        let renderer = ImageRenderer(
            content: VerseShareImageView(
                verses: verses,
                reference: reference,
                gradient: selectedGradient,
                shareFont: selectedFont,
                fontSize: fontSize
            )
        )
        renderer.scale = 3.0

        guard let image = renderer.uiImage else { return }

        let activityVC = UIActivityViewController(
            activityItems: [image],
            applicationActivities: nil
        )

        guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let rootVC = windowScene.windows.first?.rootViewController else { return }

        var presentingVC = rootVC
        while let presented = presentingVC.presentedViewController {
            presentingVC = presented
        }

        presentingVC.present(activityVC, animated: true)
    }
}
