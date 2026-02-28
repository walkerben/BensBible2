package com.bensbible.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.PresentationEntity
import com.bensbible.app.data.PresentationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PresentationsViewModel(
    private val repository: PresentationRepository
) : ViewModel() {

    val presentations: StateFlow<List<PresentationEntity>> = repository.getAllPresentations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPresentation(name: String) {
        viewModelScope.launch {
            repository.createPresentation(name)
        }
    }

    fun deletePresentation(p: PresentationEntity) {
        viewModelScope.launch {
            repository.deletePresentation(p)
        }
    }
}
