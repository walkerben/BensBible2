package com.bensbible.app.data

import kotlinx.coroutines.flow.Flow

class ReadingPlanRepository(private val dao: ReadingPlanDao) {
    fun getAll(): Flow<List<ReadingPlanProgressEntity>> = dao.getAll()

    suspend fun startPlan(planId: String) {
        if (dao.getByPlanId(planId) != null) return
        dao.upsert(ReadingPlanProgressEntity(planId = planId))
    }

    suspend fun markDayComplete(planId: String, dayNumber: Int, totalDays: Int) {
        val current = dao.getByPlanId(planId) ?: return
        val days = current.completedDays.toMutableSet().also { it.add(dayNumber) }
        val newData = days.sorted().joinToString(",")
        val completedAt = if (days.size >= totalDays) System.currentTimeMillis() else current.completedAt
        dao.upsert(current.copy(completedDaysData = newData, completedAt = completedAt))
    }

    suspend fun deletePlan(planId: String) {
        dao.getByPlanId(planId)?.let { dao.delete(it) }
    }

    suspend fun countActivePlans(): Int = dao.countActivePlans()
}
