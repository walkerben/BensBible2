import SwiftUI

struct FillBlankExerciseView: View {
    let verse: MemorizedVerse
    let segments: [FillBlankSegment]
    let onSubmit: (Int) -> Void

    @State private var answers: [Int: String] = [:]
    @State private var submitted = false
    @State private var hintUsed = false

    private var blankIndices: [Int] {
        segments.indices.compactMap { i in
            if case .blank = segments[i] { return i } else { return nil }
        }
    }

    private var allAnswered: Bool {
        blankIndices.allSatisfy { answers[$0]?.isEmpty == false }
    }

    private var isCorrect: Bool {
        blankIndices.allSatisfy { i in
            if case .blank(let answer) = segments[i] {
                let given = answers[i, default: ""].trimmingCharacters(in: .whitespacesAndNewlines)
                return given.lowercased() == answer.filter({ !",;:!?.".contains($0) }).lowercased() ||
                       given.lowercased() == answer.lowercased()
            }
            return false
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text(verse.reference)
                    .font(.title2.bold())
                    .padding(.horizontal)

                Text("Fill in the blank")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal)

                // Verse with inline blanks
                wrappedSegments
                    .padding(.horizontal)

                if submitted {
                    feedbackView
                        .padding(.horizontal)
                }

                HStack {
                    if !submitted && !hintUsed {
                        Button("Hint") {
                            hintUsed = true
                            for i in blankIndices {
                                if case .blank(let answer) = segments[i] {
                                    answers[i] = String(answer.prefix(2)) + "…"
                                }
                            }
                        }
                        .foregroundStyle(.secondary)
                    }
                    Spacer()
                    if !submitted {
                        Button("Check") {
                            submitted = true
                            let quality: Int
                            if isCorrect {
                                quality = hintUsed ? 3 : 5
                            } else {
                                quality = 1
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                onSubmit(quality)
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(!allAnswered)
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 20)
            }
            .padding(.top, 16)
        }
    }

    private var wrappedSegments: some View {
        var text = ""
        var blankCount = 0
        // Build a label for each segment
        return VStack(alignment: .leading, spacing: 8) {
            FlowLayout(spacing: 4) {
                ForEach(segments.indices, id: \.self) { i in
                    switch segments[i] {
                    case .word(let w):
                        Text(w + " ")
                            .font(.body)
                    case .blank:
                        let idx = i
                        TextField("_____", text: Binding(
                            get: { answers[idx, default: ""] },
                            set: { answers[idx] = $0 }
                        ))
                        .textFieldStyle(.roundedBorder)
                        .frame(minWidth: 80, maxWidth: 120)
                        .autocorrectionDisabled()
                        .textInputAutocapitalization(.never)
                        .overlay(
                            submitted ? (isBlankCorrect(idx) ?
                                RoundedRectangle(cornerRadius: 6).stroke(.green, lineWidth: 2) :
                                RoundedRectangle(cornerRadius: 6).stroke(.red, lineWidth: 2)
                            ) : nil
                        )
                    }
                }
            }
        }
    }

    private func isBlankCorrect(_ idx: Int) -> Bool {
        guard case .blank(let answer) = segments[idx] else { return false }
        let given = answers[idx, default: ""].trimmingCharacters(in: .whitespacesAndNewlines)
        return given.lowercased() == answer.filter({ !",;:!?.".contains($0) }).lowercased() ||
               given.lowercased() == answer.lowercased()
    }

    private var feedbackView: some View {
        VStack(alignment: .leading, spacing: 8) {
            if !isCorrect {
                Text("Correct answers:")
                    .font(.caption.bold())
                    .foregroundStyle(.secondary)
                ForEach(blankIndices, id: \.self) { i in
                    if case .blank(let answer) = segments[i] {
                        Text("• \(answer)")
                            .font(.caption)
                            .foregroundStyle(.green)
                    }
                }
            }
        }
    }
}

// Simple flow layout for wrapping words
struct FlowLayout: Layout {
    var spacing: CGFloat = 4

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? 0
        var height: CGFloat = 0
        var x: CGFloat = 0
        var rowHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > width && x > 0 {
                height += rowHeight + spacing
                x = 0
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        height += rowHeight
        return CGSize(width: width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX && x > bounds.minX {
                y += rowHeight + spacing
                x = bounds.minX
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
