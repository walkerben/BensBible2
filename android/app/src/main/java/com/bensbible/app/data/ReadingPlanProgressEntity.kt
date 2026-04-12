package com.bensbible.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reading_plan_progress")
data class ReadingPlanProgressEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val planId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val completedDaysData: String = ""
) {
    val isCompleted: Boolean get() = completedAt != null
    val completedDays: Set<Int> get() = completedDaysData
        .split(",").filter { it.isNotBlank() }.mapNotNull { it.toIntOrNull() }.toSet()
    val completedCount: Int get() = completedDays.size
}
