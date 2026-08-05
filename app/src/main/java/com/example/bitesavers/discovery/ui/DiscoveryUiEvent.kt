package com.example.bitesavers.discovery.ui

import com.example.bitesavers.discovery.data.DiscoveryCategory
import com.example.bitesavers.discovery.data.OfferUiModel
import com.example.bitesavers.discovery.data.UserRole

//For this file, it is a complete list of every single action a user can take on the Discovery Screen

//telling the android compiler, these are the only actions allowed on Discovery page
sealed interface DiscoveryUiEvent {
    data class OnSearchQueryChanged(val query: String) : DiscoveryUiEvent
    data class OnCategorySelected(val category: DiscoveryCategory) : DiscoveryUiEvent
    data class OnOfferClicked(val offer: OfferUiModel) : DiscoveryUiEvent
    data class OnRoleChanged(val role: UserRole) : DiscoveryUiEvent
    data object OnNotificationClicked : DiscoveryUiEvent
}