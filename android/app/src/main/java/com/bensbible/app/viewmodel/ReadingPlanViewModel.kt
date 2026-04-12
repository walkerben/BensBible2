package com.bensbible.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.ReadingPlan
import com.bensbible.app.data.ReadingPlanProgressEntity
import com.bensbible.app.data.ReadingPlanRepository
import com.bensbible.app.data.allReadingPlans
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadingPlanViewModel(private val repository: ReadingPlanRepository) : ViewModel() {
    val allPlans: List<ReadingPlan> = allReadingPlans

    val progressList: StateFlow<List<ReadingPlanProgressEntity>> =
        repository.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun progressFor(planId: String): ReadingPlanProgressEntity? =
        progressList.value.find { it.planId == planId }

    fun startPlan(planId: String) { viewModelScope.launch { repository.startPlan(planId) } }

    fun markDayComplete(planId: String, dayNumber: Int, totalDays: Int) {
        viewModelScope.launch { repository.markDayComplete(planId, dayNumber, totalDays) }
    }

    fun deletePlan(planId: String) { viewModelScope.launch { repository.deletePlan(planId) } }
}
