import SwiftUI
import SwiftData

struct AddToMemorizeSheet: View {
    let verses: [(bookName: String, chapterNumber: Int, verseNumber: Int, text: String)]
    let onDone: () -> Void

    @Environment(\.dismiss) private var dismiss
    @Environment(\.modelContext) private var modelContext
    @State private var viewModel: MemorizeViewModel?
    @State private var added = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Image(systemName: "brain")
                    .font(.system(size: 48))
                    .foregroundStyle(.blue)
                    .padding(.top, 24)

                Text("Add to Memorize")
                    .font(.title2.bold())

                VStack(alignment: .leading, spacing: 12) {
                    ForEach(verses, id: \.verseNumber) { verse in
                        VStack(alignment: .leading, spacing: 4) {
                            Text("\(verse.bookName) \(verse.chapterNumber):\(verse.verseNumber)")
                                .font(.headline)
                            Text(verse.text)
                                .font(.body)
                                .foregroundStyle(.secondary)
                        }
                        .padding(12)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color(.systemGray6))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
                .padding(.horizontal)

                if added {
                    Label("Added to Memorize!", systemImage: "checkmark.circle.fill")
                        .foregroundStyle(.green)
                        .font(.headline)
                }

                Spacer()

                HStack(spacing: 16) {
                    Button("Cancel") { dismiss() }
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity)

                    Button(added ? "Done" : "Add") {
                        if !added {
                            addVerses()
                        } else {
                            onDone()
                            dismiss()
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .frame(maxWidth: .infinity)
                }
                .padding(.horizontal)
                .padding(.bottom, 24)
            }
            .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear {
            let vm = MemorizeViewModel()
            vm.configure(modelContext: modelContext)
            viewModel = vm
        }
    }

    private func addVerses() {
        guard let vm = viewModel else { return }
        for verse in verses {
            vm.addVerse(
                bookName: verse.bookName,
                chapterNumber: verse.chapterNumber,
                verseNumber: verse.verseNumber,
                verseText: verse.text
            )
        }
        added = true
    }
}
