package com.bensbible.app.model

import com.bensbible.app.data.MemorizedVerseEntity

sealed class ExerciseState {
    abstract val verse: MemorizedVerseEntity

    data class FillBlank(
        override val verse: MemorizedVerseEntity,
        val segments: List<FillBlankSegment>
    ) : ExerciseState()

    data class WordDrag(
        override val verse: MemorizedVerseEntity,
        val shuffledWords: List<String>,
        val correctOrder: List<String>
    ) : ExerciseState()

    data class MultipleChoice(
        override val verse: MemorizedVerseEntity,
        val options: List<String>
    ) : ExerciseState()
}

sealed class FillBlankSegment {
    data class Word(val text: String) : FillBlankSegment()
    data class Blank(val answer: String) : FillBlankSegment()
}

enum class ExerciseType {
    FILL_BLANK, WORD_DRAG, MULTIPLE_CHOICE
}
