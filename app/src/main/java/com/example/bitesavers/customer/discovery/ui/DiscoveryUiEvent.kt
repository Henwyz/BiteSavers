package com.example.bitesavers.customer.discovery.ui

import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

//For this file, it is a complete list of every single action a user can take on the Discovery Screen

//telling the android compiler, these are the only actions allowed on Discovery page
sealed class DiscoveryUiEvent {
    data class OnSearchQueryChanged(val query: String) : DiscoveryUiEvent()
    data class OnCategorySelected(val category: DiscoveryCategory) : DiscoveryUiEvent()
    data class OnOfferClicked(val offer: OfferUiModel) : DiscoveryUiEvent()
    data class OnRoleChanged(val role: UserRole) : DiscoveryUiEvent()
    object OnNotificationClicked : DiscoveryUiEvent()

    data class OnMapMarkerClicked(val offerId: String?) : DiscoveryUiEvent()
    data class OnMapOfferNavigate(val offerId: String) : DiscoveryUiEvent()
}