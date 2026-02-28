package com.bensbible.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "presentation_slides",
    foreignKeys = [
        ForeignKey(
            entity = PresentationEntity::class,
            parentColumns = ["id"],
            childColumns = ["presentationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("presentationId")]
)
data class PresentationSlideEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val presentationId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseText: String,
    val order: Int
) {
    val reference: String get() = "$bookName $chapterNumber:$verseNumber"
}
