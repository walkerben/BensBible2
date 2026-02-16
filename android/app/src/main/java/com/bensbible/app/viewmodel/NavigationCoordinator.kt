package com.bensbible.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.bensbible.app.model.AppTab
import com.bensbible.app.model.BibleLocation

class NavigationCoordinator : ViewModel() {
    var selectedTab by mutableStateOf(AppTab.READ)
    var pendingNavigation by mutableStateOf<BibleLocation?>(null)

    fun navigateToReader(location: BibleLocation) {
        pendingNavigation = location
        selectedTab = AppTab.READ
    }
}
