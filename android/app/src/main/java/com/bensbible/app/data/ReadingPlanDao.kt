package com.bensbible.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingPlanDao {
    @Query("SELECT * FROM reading_plan_progress ORDER BY startedAt DESC")
    fun getAll(): Flow<List<ReadingPlanProgressEntity>>

    @Query("SELECT * FROM reading_plan_progress WHERE planId = :planId LIMIT 1")
    suspend fun getByPlanId(planId: String): ReadingPlanProgressEntity?

    @Query("SELECT COUNT(*) FROM reading_plan_progress WHERE completedAt IS NULL")
    suspend fun countActivePlans(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: ReadingPlanProgressEntity)

    @Delete
    suspend fun delete(progress: ReadingPlanProgressEntity)
}
