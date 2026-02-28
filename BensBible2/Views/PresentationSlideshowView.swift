import SwiftUI

struct PresentationSlideshowView: View {
    let slides: [PresentationSlide]
    @Environment(\.dismiss) private var dismiss
    @State private var currentIndex = 0

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            if slides.isEmpty {
                Text("No slides")
                    .foregroundStyle(.white)
            } else {
                let slide = slides[currentIndex]

                VStack(spacing: 32) {
                    Spacer()

                    ProgressView(value: Double(currentIndex + 1), total: Double(slides.count))
                        .tint(.white)
                        .padding(.horizontal, 40)

                    Text(slide.reference)
                        .font(.title3)
                        .fontWeight(.semibold)
                        .foregroundStyle(.white.opacity(0.7))
                        .multilineTextAlignment(.center)

                    Text(slide.verseText)
                        .font(.title2)
                        .foregroundStyle(.white)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                        .lineSpacing(6)

                    Spacer()

                    HStack(spacing: 40) {
                        Button {
                            withAnimation {
                                currentIndex -= 1
                            }
                        } label: {
                            Image(systemName: "chevron.left.circle.fill")
                                .font(.system(size: 44))
                                .foregroundStyle(.white.opacity(currentIndex == 0 ? 0.3 : 0.8))
                        }
                        .disabled(currentIndex == 0)

                        Text("\(currentIndex + 1) / \(slides.count)")
                            .font(.caption)
                            .foregroundStyle(.white.opacity(0.5))

                        Button {
                            withAnimation {
                                currentIndex += 1
                            }
                        } label: {
                            Image(systemName: "chevron.right.circle.fill")
                                .font(.system(size: 44))
                                .foregroundStyle(.white.opacity(currentIndex == slides.count - 1 ? 0.3 : 0.8))
                        }
                        .disabled(currentIndex == slides.count - 1)
                    }
                    .padding(.bottom, 40)
                }
            }

            VStack {
                HStack {
                    Spacer()
                    Button("Done") {
                        dismiss()
                    }
                    .foregroundStyle(.white)
                    .padding(20)
                }
                Spacer()
            }
        }
    }
}
