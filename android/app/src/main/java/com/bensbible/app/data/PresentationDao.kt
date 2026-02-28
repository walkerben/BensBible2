package com.bensbible.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PresentationDao {

    @Query("SELECT * FROM presentations ORDER BY createdAt DESC")
    fun getAllPresentations(): Flow<List<PresentationEntity>>

    @Query("SELECT * FROM presentation_slides WHERE presentationId = :presentationId ORDER BY `order` ASC")
    fun getSlidesForPresentation(presentationId: String): Flow<List<PresentationSlideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresentation(p: PresentationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlide(s: PresentationSlideEntity)

    @Delete
    suspend fun deletePresentation(p: PresentationEntity)

    @Delete
    suspend fun deleteSlide(s: PresentationSlideEntity)

    @Update
    suspend fun updateSlide(s: PresentationSlideEntity)

    @Query("SELECT COUNT(*) FROM presentations")
    suspend fun getPresentationCount(): Int
}
