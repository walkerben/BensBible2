package com.bensbible.app.util

import kotlin.math.max
import kotlin.math.roundToInt

data class Sm2Result(
    val repetitions: Int,
    val easeFactor: Double,
    val intervalDays: Int,
    val nextReviewDate: Long
)

object Sm2Scheduler {
    fun process(quality: Int, repetitions: Int, easeFactor: Double, intervalDays: Int): Sm2Result {
        var newRepetitions = repetitions
        var newEaseFactor = easeFactor
        var newInterval = intervalDays

        if (quality >= 3) {
            newRepetitions += 1
            newInterval = when (newRepetitions) {
                1 -> 1
                2 -> 6
                else -> (intervalDays * newEaseFactor).roundToInt()
            }
            val delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)
            newEaseFactor = max(1.3, newEaseFactor + delta)
        } else {
            newRepetitions = 0
            newInterval = 1
        }

        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            add(java.util.Calendar.DAY_OF_YEAR, newInterval)
        }
        val nextDate = cal.timeInMillis
        return Sm2Result(
            repetitions = newRepetitions,
            easeFactor = newEaseFactor,
            intervalDays = newInterval,
            nextReviewDate = nextDate
        )
    }
}
