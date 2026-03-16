import SwiftUI

struct WordDragExerciseView: View {
    let verse: MemorizedVerse
    let shuffledWords: [String]
    let correctOrder: [String]
    let onSubmit: (Int) -> Void

    @State private var arrangedWords: [String] = []
    @State private var bankWords: [String] = []
    @State private var submitted = false
    @State private var hintUsed = false
    @State private var rearrangeCount = 0

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text(verse.reference)
                    .font(.title2.bold())
                    .padding(.horizontal)

                Text("Arrange the words")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal)

                // Answer zone
                VStack(alignment: .leading, spacing: 8) {
                    Text("Your answer:")
                        .font(.caption.bold())
                        .foregroundStyle(.secondary)

                    if arrangedWords.isEmpty {
                        Text("Tap words below to arrange them")
                            .font(.body)
                            .foregroundStyle(.tertiary)
                            .frame(maxWidth: .infinity, minHeight: 44, alignment: .leading)
                            .padding(.horizontal, 12)
                            .background(Color(.systemGray6))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    } else {
                        FlowLayout(spacing: 6) {
                            ForEach(arrangedWords.indices, id: \.self) { i in
                                let word = arrangedWords[i]
                                Button {
                                    if !submitted {
                                        arrangedWords.remove(at: i)
                                        bankWords.append(word)
                                        rearrangeCount += 1
                                    }
                                } label: {
                                    Text(word)
                                        .font(.body)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 6)
                                        .background(submitted ? (isCorrectAtIndex(i) ? Color.green.opacity(0.2) : Color.red.opacity(0.2)) : Color.blue.opacity(0.15))
                                        .foregroundStyle(submitted ? (isCorrectAtIndex(i) ? .green : .red) : .blue)
                                        .clipShape(RoundedRectangle(cornerRadius: 8))
                                }
                            }
                        }
                    }
                }
                .padding(.horizontal)

                Divider().padding(.horizontal)

                // Word bank
                VStack(alignment: .leading, spacing: 8) {
                    Text("Word bank:")
                        .font(.caption.bold())
                        .foregroundStyle(.secondary)

                    FlowLayout(spacing: 6) {
                        ForEach(bankWords.indices, id: \.self) { i in
                            let word = bankWords[i]
                            Button {
                                if !submitted {
                                    bankWords.remove(at: i)
                                    arrangedWords.append(word)
                                }
                            } label: {
                                Text(word)
                                    .font(.body)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 6)
                                    .background(Color(.systemGray5))
                                    .foregroundStyle(.primary)
                                    .clipShape(RoundedRectangle(cornerRadius: 8))
                            }
                        }
                    }
                }
                .padding(.horizontal)

                if submitted && !isFullyCorrect {
                    Text("Correct: \(correctOrder.joined(separator: " "))")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .padding(.horizontal)
                }

                HStack {
                    if !submitted && !arrangedWords.isEmpty {
                        Button("Clear") {
                            bankWords = shuffledWords
                            arrangedWords = []
                            rearrangeCount += 1
                        }
                        .foregroundStyle(.secondary)
                    }
                    Spacer()
                    if !submitted {
                        Button("Check") {
                            submitted = true
                            let quality: Int
                            if isFullyCorrect {
                                quality = rearrangeCount == 0 ? 5 : hintUsed ? 3 : 4
                            } else {
                                quality = 1
                            }
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                onSubmit(quality)
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(arrangedWords.count != correctOrder.count)
                    }
                }
                .padding(.horizontal)
                .padding(.bottom, 20)
            }
            .padding(.top, 16)
        }
        .onAppear {
            bankWords = shuffledWords
            arrangedWords = []
        }
    }

    private var isFullyCorrect: Bool {
        arrangedWords == correctOrder
    }

    private func isCorrectAtIndex(_ i: Int) -> Bool {
        guard i < correctOrder.count else { return false }
        return arrangedWords[i] == correctOrder[i]
    }
}
