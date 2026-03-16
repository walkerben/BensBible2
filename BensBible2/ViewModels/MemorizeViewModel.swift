import Foundation
import SwiftData

@Observable
final class MemorizeViewModel {
    private var modelContext: ModelContext?

    var memorizedVerses: [MemorizedVerse] = []
    var dueVerses: [MemorizedVerse] = []

    // Session state
    var sessionQueue: [MemorizedVerse] = []
    var sessionIndex: Int = 0
    var sessionComplete: Bool = false
    var sessionCorrect: Int = 0
    var sessionTotal: Int = 0
    var currentExercise: ExerciseState?
    var showingResult: Bool = false
    var lastResultWasCorrect: Bool = false
    private var lastExerciseType: ExerciseType?

    func configure(modelContext: ModelContext) {
        self.modelContext = modelContext
        load()
    }

    func load() {
        guard let context = modelContext else { return }
        let descriptor = FetchDescriptor<MemorizedVerse>(sortBy: [SortDescriptor(\.addedAt)])
        memorizedVerses = (try? context.fetch(descriptor)) ?? []
        dueVerses = memorizedVerses.filter { $0.isDue }
    }

    func addVerse(bookName: String, chapterNumber: Int, verseNumber: Int, verseText: String) {
        guard let context = modelContext else { return }
        let key = "\(bookName) \(chapterNumber):\(verseNumber)"
        guard !memorizedVerses.contains(where: { $0.verseKey == key }) else { return }
        let verse = MemorizedVerse(bookName: bookName, chapterNumber: chapterNumber, verseNumber: verseNumber, verseText: verseText)
        context.insert(verse)
        load()
    }

    func removeVerse(_ verse: MemorizedVerse) {
        guard let context = modelContext else { return }
        context.delete(verse)
        load()
    }

    func startSession() {
        sessionQueue = dueVerses.shuffled()
        sessionIndex = 0
        sessionCorrect = 0
        sessionTotal = 0
        sessionComplete = false
        lastExerciseType = nil
        advanceToNextExercise()
    }

    func submitAnswer(quality: Int) {
        guard let context = modelContext,
              let exercise = currentExercise else { return }
        let verse = exercise.verse

        lastResultWasCorrect = quality >= 3
        sessionTotal += 1
        if quality >= 4 { sessionCorrect += 1 }

        let log = MemoryReviewLog(verseKey: verse.verseKey, quality: quality, exerciseType: exercise.exerciseType.rawValue)
        context.insert(log)

        let result = SM2Scheduler.process(
            quality: quality,
            repetitions: verse.repetitions,
            easeFactor: verse.easeFactor,
            intervalDays: verse.intervalDays
        )
        verse.repetitions = result.repetitions
        verse.easeFactor = result.easeFactor
        verse.intervalDays = result.intervalDays
        verse.nextReviewDate = result.nextReviewDate
        verse.lastReviewedAt = Date()
        verse.totalReviews += 1

        lastExerciseType = exercise.exerciseType
        showingResult = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { [weak self] in
            self?.showingResult = false
            self?.moveToNext()
        }
    }

    func skipVerse() {
        moveToNext()
    }

    // MARK: - Private

    private func moveToNext() {
        sessionIndex += 1
        if sessionIndex >= sessionQueue.count {
            sessionComplete = true
            currentExercise = nil
            load()
        } else {
            advanceToNextExercise()
        }
    }

    private func advanceToNextExercise() {
        guard sessionIndex < sessionQueue.count else {
            sessionComplete = true
            return
        }
        let verse = sessionQueue[sessionIndex]
        currentExercise = generateExercise(for: verse)
    }

    private func generateExercise(for verse: MemorizedVerse) -> ExerciseState {
        let type = selectExerciseType(for: verse)
        lastExerciseType = type
        switch type {
        case .multipleChoice: return makeMultipleChoice(verse: verse)
        case .fillBlank: return makeFillBlank(verse: verse)
        case .wordDrag: return makeWordDrag(verse: verse)
        }
    }

    private func selectExerciseType(for verse: MemorizedVerse) -> ExerciseType {
        if verse.repetitions == 0 { return .multipleChoice }
        if verse.repetitions == 1 { return .fillBlank }
        let all: [ExerciseType] = [.fillBlank, .wordDrag, .multipleChoice]
        let available = lastExerciseType.map { last in all.filter { $0 != last } } ?? all
        return available.randomElement() ?? .fillBlank
    }

    private func makeFillBlank(verse: MemorizedVerse) -> ExerciseState {
        let words = verse.verseText.components(separatedBy: " ")
        let count = words.count
        let blankCount = count < 10 ? 1 : count < 20 ? 2 : 3
        let step = max(1, count / (blankCount + 1))

        var blankIndices = Set<Int>()
        for i in 1...blankCount {
            var idx = min(i * step, count - 1)
            for offset in 0..<5 {
                let candidate = (idx + offset) % count
                let stripped = words[candidate].filter { $0.isLetter }
                if stripped.count > 3 { idx = candidate; break }
            }
            blankIndices.insert(idx)
        }

        let segments: [FillBlankSegment] = words.enumerated().map { i, word in
            blankIndices.contains(i) ? .blank(answer: word) : .word(word)
        }
        return .fillBlank(verse: verse, segments: segments)
    }

    private func makeWordDrag(verse: MemorizedVerse) -> ExerciseState {
        let words = verse.verseText.components(separatedBy: " ")
        var shuffled = words.shuffled()
        if shuffled == words && words.count > 1 { shuffled = words.shuffled() }
        return .wordDrag(verse: verse, shuffledWords: shuffled, correctOrder: words)
    }

    private func makeMultipleChoice(verse: MemorizedVerse) -> ExerciseState {
        let distractors = memorizedVerses
            .filter { $0.id != verse.id }
            .shuffled()
            .prefix(3)
            .map { $0.verseText }
        var options = [verse.verseText] + distractors
        let fallbacks = [
            "For the wages of sin is death; but the gift of God is eternal life through Jesus Christ our Lord.",
            "For all have sinned, and come short of the glory of God;",
            "That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved."
        ]
        var fi = 0
        while options.count < 4 { options.append(fallbacks[fi % fallbacks.count]); fi += 1 }
        return .multipleChoice(verse: verse, options: Array(options.prefix(4)).shuffled())
    }

    func seedDefaultVersesIfNeeded() {
        guard let context = modelContext else { return }
        let descriptor = FetchDescriptor<MemorizedVerse>()
        let count = (try? context.fetch(descriptor).count) ?? 0
        guard count == 0 else { return }

        let defaults: [(String, Int, Int, String)] = [
            ("John", 3, 16, "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life."),
            ("Jeremiah", 29, 11, "For I know the thoughts that I think toward you, saith the LORD, thoughts of peace, and not of evil, to give you an expected end."),
            ("Philippians", 4, 13, "I can do all things through Christ which strengtheneth me."),
            ("Romans", 8, 28, "And we know that all things work together for good to them that love God, to them who are the called according to his purpose."),
            ("Proverbs", 3, 5, "Trust in the LORD with all thine heart; and lean not unto thine own understanding."),
            ("Isaiah", 40, 31, "But they that wait upon the LORD shall renew their strength; they shall mount up with wings as eagles; they shall run, and not be weary; and they shall walk, and not faint."),
            ("Joshua", 1, 9, "Have not I commanded thee? Be strong and of a good courage; be not afraid, neither be thou dismayed: for the LORD thy God is with thee whithersoever thou goest.")
        ]

        for (book, chapter, verse, text) in defaults {
            let mv = MemorizedVerse(bookName: book, chapterNumber: chapter, verseNumber: verse, verseText: text)
            mv.nextReviewDate = Date()
            context.insert(mv)
        }
        load()
    }
}
