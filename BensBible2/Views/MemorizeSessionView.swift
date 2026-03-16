import SwiftUI

struct MemorizeSessionView: View {
    let viewModel: MemorizeViewModel
    let onDone: () -> Void

    var body: some View {
        NavigationStack {
            ZStack {
                if viewModel.sessionComplete {
                    SessionCompleteView(
                        correct: viewModel.sessionCorrect,
                        total: viewModel.sessionTotal,
                        onDone: onDone
                    )
                } else if let exercise = viewModel.currentExercise {
                    VStack(spacing: 0) {
                        progressBar
                        exerciseContent(exercise)
                        Spacer()
                    }
                } else {
                    ProgressView()
                }

                if viewModel.showingResult {
                    resultOverlay
                }
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Close") { onDone() }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Skip") { viewModel.skipVerse() }
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    private var progressBar: some View {
        let total = viewModel.sessionQueue.count
        let current = viewModel.sessionIndex
        let progress = total > 0 ? Double(current) / Double(total) : 0
        return ProgressView(value: progress)
            .padding(.horizontal)
            .padding(.vertical, 8)
    }

    @ViewBuilder
    private func exerciseContent(_ exercise: ExerciseState) -> some View {
        switch exercise {
        case .fillBlank(let verse, let segments):
            FillBlankExerciseView(verse: verse, segments: segments) { quality in
                viewModel.submitAnswer(quality: quality)
            }
            .id(verse.id)
        case .wordDrag(let verse, let shuffled, let correct):
            WordDragExerciseView(verse: verse, shuffledWords: shuffled, correctOrder: correct) { quality in
                viewModel.submitAnswer(quality: quality)
            }
            .id(verse.id)
        case .multipleChoice(let verse, let options):
            MultipleChoiceExerciseView(verse: verse, options: options) { quality in
                viewModel.submitAnswer(quality: quality)
            }
            .id(verse.id)
        }
    }

    private var resultOverlay: some View {
        VStack {
            Spacer()
            HStack {
                Image(systemName: viewModel.lastResultWasCorrect ? "checkmark.circle.fill" : "xmark.circle.fill")
                    .font(.title2)
                Text(viewModel.lastResultWasCorrect ? "Correct!" : "Keep practicing")
                    .font(.headline)
            }
            .foregroundStyle(viewModel.lastResultWasCorrect ? .green : .red)
            .padding()
            .background(.regularMaterial)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .padding(.bottom, 32)
        }
        .transition(.move(edge: .bottom).combined(with: .opacity))
        .animation(.spring(duration: 0.3), value: viewModel.showingResult)
    }
}

struct SessionCompleteView: View {
    let correct: Int
    let total: Int
    let onDone: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "star.fill")
                .font(.system(size: 64))
                .foregroundStyle(.yellow)

            Text("Session Complete!")
                .font(.title.bold())

            Text("\(correct) / \(total) correct")
                .font(.title2)
                .foregroundStyle(.secondary)

            if total > 0 {
                let pct = Int(Double(correct) / Double(total) * 100)
                Text("\(pct)% accuracy")
                    .font(.headline)
                    .foregroundStyle(pct >= 80 ? .green : pct >= 50 ? .orange : .red)
            }

            Button("Done") { onDone() }
                .buttonStyle(.borderedProminent)
                .padding(.top, 8)
        }
        .padding()
    }
}
