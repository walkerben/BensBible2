package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.MemorizeRepository
import com.bensbible.app.data.MemorizedVerseEntity
import com.bensbible.app.model.ExerciseState
import com.bensbible.app.model.ExerciseType
import com.bensbible.app.model.FillBlankSegment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemorizeViewModel(
    private val repository: MemorizeRepository
) : ViewModel() {

    val allVerses: StateFlow<List<MemorizedVerseEntity>> = repository.getAllVerses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dueVerses: StateFlow<List<MemorizedVerseEntity>> = repository.getAllVerses()
        .map { list -> list.filter { it.isDue } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Session state
    var sessionActive by mutableStateOf(false)
        private set
    var currentExercise by mutableStateOf<ExerciseState?>(null)
        private set
    var sessionComplete by mutableStateOf(false)
        private set
    var sessionCorrect by mutableStateOf(0)
        private set
    var sessionTotal by mutableStateOf(0)
        private set
    var showingResult by mutableStateOf(false)
        private set
    var lastResultWasCorrect by mutableStateOf(false)
        private set

    var sessionQueueSize by mutableStateOf(0)
        private set
    var sessionIndex by mutableStateOf(0)
        private set
    private var sessionQueue: List<MemorizedVerseEntity> = emptyList()
    private var lastExerciseType: ExerciseType? = null

    fun addVerse(bookName: String, chapterNumber: Int, verseNumber: Int, verseText: String) {
        viewModelScope.launch {
            repository.addVerse(bookName, chapterNumber, verseNumber, verseText)
        }
    }

    fun deleteVerse(verse: MemorizedVerseEntity) {
        viewModelScope.launch {
            repository.deleteVerse(verse)
        }
    }

    fun startSession(due: List<MemorizedVerseEntity>) {
        sessionQueue = due.shuffled()
        sessionQueueSize = sessionQueue.size
        sessionIndex = 0
        sessionCorrect = 0
        sessionTotal = 0
        sessionComplete = false
        lastExerciseType = null
        sessionActive = true
        advanceToNextExercise()
    }

    fun submitAnswer(quality: Int) {
        val exercise = currentExercise ?: return
        val verse = exercise.verse

        lastResultWasCorrect = quality >= 3
        sessionTotal++
        if (quality >= 4) sessionCorrect++

        val (exerciseTypeName, exerciseTypeEnum) = when (exercise) {
            is ExerciseState.FillBlank -> ExerciseType.FILL_BLANK.name to ExerciseType.FILL_BLANK
            is ExerciseState.WordDrag -> ExerciseType.WORD_DRAG.name to ExerciseType.WORD_DRAG
            is ExerciseState.MultipleChoice -> ExerciseType.MULTIPLE_CHOICE.name to ExerciseType.MULTIPLE_CHOICE
        }
        lastExerciseType = exerciseTypeEnum

        viewModelScope.launch {
            repository.applyReview(verse, quality, exerciseTypeName)
        }

        showingResult = true
        viewModelScope.launch {
            delay(1500)
            showingResult = false
            moveToNext()
        }
    }

    fun skipVerse() {
        moveToNext()
    }

    fun endSession() {
        sessionActive = false
        sessionComplete = false
        currentExercise = null
    }

    // MARK: - Private

    private fun moveToNext() {
        val next = sessionIndex + 1
        sessionIndex = next
        if (next >= sessionQueue.size) {
            sessionComplete = true
            currentExercise = null
        } else {
            advanceToNextExercise()
        }
    }

    private fun advanceToNextExercise() {
        val verse = sessionQueue.getOrNull(sessionIndex) ?: run {
            sessionComplete = true
            return
        }
        currentExercise = generateExercise(verse, allVerses.value)
    }

    private fun generateExercise(
        verse: MemorizedVerseEntity,
        allVerses: List<MemorizedVerseEntity>
    ): ExerciseState {
        val type = selectExerciseType(verse)
        lastExerciseType = type
        return when (type) {
            ExerciseType.MULTIPLE_CHOICE -> makeMultipleChoice(verse, allVerses)
            ExerciseType.FILL_BLANK -> makeFillBlank(verse)
            ExerciseType.WORD_DRAG -> makeWordDrag(verse)
        }
    }

    private fun selectExerciseType(verse: MemorizedVerseEntity): ExerciseType {
        return when (verse.repetitions) {
            0 -> ExerciseType.MULTIPLE_CHOICE
            1 -> ExerciseType.FILL_BLANK
            else -> {
                val all = ExerciseType.values().toList()
                val available = lastExerciseType?.let { last -> all.filter { it != last } } ?: all
                available.random()
            }
        }
    }

    private fun makeFillBlank(verse: MemorizedVerseEntity): ExerciseState.FillBlank {
        val words = verse.verseText.split(" ")
        val count = words.size
        val blankCount = when {
            count < 10 -> 1
            count < 20 -> 2
            else -> 3
        }
        val step = maxOf(1, count / (blankCount + 1))
        val blankIndices = mutableSetOf<Int>()
        for (i in 1..blankCount) {
            var idx = minOf(i * step, count - 1)
            for (offset in 0 until 5) {
                val candidate = (idx + offset) % count
                if (words[candidate].count { it.isLetter() } > 3) {
                    idx = candidate
                    break
                }
            }
            blankIndices.add(idx)
        }
        val segments = words.mapIndexed { i, word ->
            if (blankIndices.contains(i)) FillBlankSegment.Blank(word)
            else FillBlankSegment.Word(word)
        }
        return ExerciseState.FillBlank(verse, segments)
    }

    private fun makeWordDrag(verse: MemorizedVerseEntity): ExerciseState.WordDrag {
        val words = verse.verseText.split(" ")
        var shuffled = words.shuffled()
        if (shuffled == words && words.size > 1) shuffled = words.shuffled()
        return ExerciseState.WordDrag(verse, shuffled, words)
    }

    private fun makeMultipleChoice(
        verse: MemorizedVerseEntity,
        allVerses: List<MemorizedVerseEntity>
    ): ExerciseState.MultipleChoice {
        val distractors = allVerses
            .filter { it.id != verse.id }
            .shuffled()
            .take(3)
            .map { it.verseText }
        val fallbacks = listOf(
            "For the wages of sin is death; but the gift of God is eternal life through Jesus Christ our Lord.",
            "For all have sinned, and come short of the glory of God;",
            "That if thou shalt confess with thy mouth the Lord Jesus, and shalt believe in thine heart that God hath raised him from the dead, thou shalt be saved."
        )
        val options = mutableListOf(verse.verseText)
        options.addAll(distractors)
        var fi = 0
        while (options.size < 4) { options.add(fallbacks[fi++ % fallbacks.size]) }
        return ExerciseState.MultipleChoice(verse, options.take(4).shuffled())
    }
}
