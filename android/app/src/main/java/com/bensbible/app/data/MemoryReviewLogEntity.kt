package com.bensbible.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "memory_review_logs")
data class MemoryReviewLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val verseKey: String,
    val reviewedAt: Long = System.currentTimeMillis(),
    val quality: Int,
    val exerciseType: String
)
