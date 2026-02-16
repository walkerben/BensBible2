import SwiftUI

struct BookChapterPickerView: View {
    @Bindable var viewModel: ReaderViewModel
    @State private var selectedBook: String?
    @Environment(\.dismiss) private var dismiss

    private let oldTestamentCount = 39

    var body: some View {
        NavigationStack {
            Group {
                if let book = selectedBook {
                    chapterGrid(for: book)
                } else {
                    bookList
                }
            }
            .navigationTitle(selectedBook ?? "Books")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    if selectedBook != nil {
                        Button("Back") {
                            selectedBook = nil
                        }
                    } else {
                        Button("Cancel") {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private var bookList: some View {
        List {
            Section("Old Testament") {
                ForEach(Array(viewModel.bookNames.prefix(oldTestamentCount)), id: \.self) { name in
                    bookRow(name)
                }
            }
            Section("New Testament") {
                ForEach(Array(viewModel.bookNames.dropFirst(oldTestamentCount)), id: \.self) { name in
                    bookRow(name)
                }
            }
        }
    }

    private func bookRow(_ name: String) -> some View {
        Button {
            selectedBook = name
        } label: {
            HStack {
                Text(name)
                    .foregroundStyle(.primary)
                Spacer()
                if name == viewModel.currentLocation.bookName {
                    Image(systemName: "checkmark")
                        .foregroundStyle(Color.accentColor)
                }
            }
        }
    }

    private func chapterGrid(for book: String) -> some View {
        let count = viewModel.chapterCount(for: book)
        let columns = Array(repeating: GridItem(.flexible(), spacing: 12), count: 5)

        return ScrollView {
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(1...max(count, 1), id: \.self) { number in
                    Button {
                        viewModel.navigateTo(book: book, chapter: number)
                        dismiss()
                    } label: {
                        Text("\(number)")
                            .font(.body)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .fill(isCurrentChapter(book: book, chapter: number)
                                          ? Color.accentColor
                                          : Color(.systemGray5))
                            )
                            .foregroundStyle(
                                isCurrentChapter(book: book, chapter: number)
                                ? .white
                                : .primary
                            )
                    }
                }
            }
            .padding()
        }
    }

    private func isCurrentChapter(book: String, chapter: Int) -> Bool {
        viewModel.currentLocation.bookName == book &&
        viewModel.currentLocation.chapterNumber == chapter
    }
}
