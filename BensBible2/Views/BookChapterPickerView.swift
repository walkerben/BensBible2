import SwiftUI

struct BookChapterPickerView: View {
    @Bindable var viewModel: ReaderViewModel
    @State private var selectedBook: String?
    @State private var selectedGroup: BookGroup = .all
    @Environment(\.dismiss) private var dismiss

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
        VStack(spacing: 0) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(BookGroup.searchFilters) { group in
                        Button {
                            selectedGroup = group
                        } label: {
                            Text(group.displayName)
                                .font(.subheadline)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(
                                    Capsule()
                                        .fill(selectedGroup == group
                                              ? Color.accentColor
                                              : Color(.systemGray5))
                                )
                                .foregroundStyle(selectedGroup == group
                                                 ? .white
                                                 : .primary)
                        }
                    }
                }
                .padding(.horizontal)
                .padding(.vertical, 8)
            }
            List {
                if selectedGroup == .all {
                    ForEach(BookGroup.pickerSections) { group in
                        Section(group.displayName) {
                            ForEach(group.filterBooks(from: viewModel.bookNames), id: \.self) { name in
                                bookRow(name)
                            }
                        }
                    }
                } else {
                    ForEach(selectedGroup.filterBooks(from: viewModel.bookNames), id: \.self) { name in
                        bookRow(name)
                    }
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
