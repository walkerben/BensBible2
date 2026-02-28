package com.bensbible.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bensbible.app.data.PresentationSlideEntity
import com.bensbible.app.data.PresentationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PresentationDetailViewModel(
    private val repository: PresentationRepository,
    presentationId: String
) : ViewModel() {

    val slides: StateFlow<List<PresentationSlideEntity>> = repository.getSlidesForPresentation(presentationId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSlide(s: PresentationSlideEntity) {
        viewModelScope.launch {
            repository.deleteSlide(s)
        }
    }

    fun reorderSlide(from: Int, to: Int, currentSlides: List<PresentationSlideEntity>) {
        viewModelScope.launch {
            val mutable = currentSlides.toMutableList()
            val item = mutable.removeAt(from)
            mutable.add(to, item)
            mutable.forEachIndexed { index, slide ->
                if (slide.order != index) {
                    repository.updateSlide(slide.copy(order = index))
                }
            }
        }
    }
}
