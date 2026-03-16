package com.bensbible.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemorizeDao {

    @Query("SELECT * FROM memorized_verses ORDER BY addedAt ASC")
    fun getAllVerses(): Flow<List<MemorizedVerseEntity>>

    @Query("SELECT COUNT(*) FROM memorized_verses")
    suspend fun getCount(): Int

    @Query("SELECT * FROM memorized_verses ORDER BY addedAt ASC")
    suspend fun getAllVersesSnapshot(): List<MemorizedVerseEntity>

    @Query("SELECT * FROM memorized_verses WHERE id = :id")
    suspend fun getById(id: String): MemorizedVerseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVerse(verse: MemorizedVerseEntity)

    @Update
    suspend fun updateVerse(verse: MemorizedVerseEntity)

    @Delete
    suspend fun deleteVerse(verse: MemorizedVerseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MemoryReviewLogEntity)
}
