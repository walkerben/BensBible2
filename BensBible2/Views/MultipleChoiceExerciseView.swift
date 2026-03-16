import SwiftUI

struct MultipleChoiceExerciseView: View {
    let verse: MemorizedVerse
    let options: [String]
    let onSubmit: (Int) -> Void

    @State private var selectedOption: String?
    @State private var submitted = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text(verse.reference)
                    .font(.title2.bold())
                    .padding(.horizontal)

                Text("Which verse matches this reference?")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .padding(.horizontal)

                VStack(spacing: 12) {
                    ForEach(options, id: \.self) { option in
                        Button {
                            guard !submitted else { return }
                            selectedOption = option
                            submitted = true
                            let isCorrect = option == verse.verseText
                            let quality = isCorrect ? 5 : 1
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
                                onSubmit(quality)
                            }
                        } label: {
                            HStack(alignment: .top, spacing: 12) {
                                optionIcon(for: option)
                                Text(option)
                                    .font(.body)
                                    .multilineTextAlignment(.leading)
                                    .foregroundStyle(.primary)
                                Spacer()
                            }
                            .padding(14)
                            .background(optionBackground(for: option))
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(optionBorderColor(for: option), lineWidth: submitted ? 2 : 1)
                            )
                        }
                    }
                }
                .padding(.horizontal)
            }
            .padding(.top, 16)
        }
    }

    private func optionBackground(for option: String) -> Color {
        guard submitted else { return Color(.systemGray6) }
        if option == verse.verseText { return .green.opacity(0.15) }
        if option == selectedOption { return .red.opacity(0.15) }
        return Color(.systemGray6)
    }

    private func optionBorderColor(for option: String) -> Color {
        guard submitted else { return Color(.systemGray4) }
        if option == verse.verseText { return .green }
        if option == selectedOption { return .red }
        return Color(.systemGray4)
    }

    @ViewBuilder
    private func optionIcon(for option: String) -> some View {
        if submitted {
            if option == verse.verseText {
                Image(systemName: "checkmark.circle.fill")
                    .foregroundStyle(.green)
            } else if option == selectedOption {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(.red)
            } else {
                Image(systemName: "circle")
                    .foregroundStyle(.secondary)
            }
        } else {
            Image(systemName: selectedOption == option ? "circle.inset.filled" : "circle")
                .foregroundStyle(.blue)
        }
    }
}
