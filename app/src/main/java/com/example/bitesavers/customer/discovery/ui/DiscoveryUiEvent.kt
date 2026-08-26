package com.example.bitesavers.customer.discovery.ui

import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

sealed class DiscoveryUiEvent {
    data class OnSearchQueryChanged(val query: String) : DiscoveryUiEvent()
    data class OnCategorySelected(val category: DiscoveryCategory) : DiscoveryUiEvent()
    data class OnOfferClicked(val offer: OfferUiModel) : DiscoveryUiEvent()
    data class OnToggleBookmark(val offerId: String) : DiscoveryUiEvent() // 👈 Added
    data class OnRoleChanged(val role: UserRole) : DiscoveryUiEvent()
    object OnNotificationClicked : DiscoveryUiEvent()

    data class OnMapMarkerClicked(val offerId: String?) : DiscoveryUiEvent()
    data class OnMapOfferNavigate(val offerId: String) : DiscoveryUiEvent()

    object OnResetFilters : DiscoveryUiEvent()
}