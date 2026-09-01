package com.example.bitesavers.customer.discovery.ui

import com.example.bitesavers.customer.discovery.data.DiscoveryStoreUiModel
import com.example.bitesavers.customer.discovery.data.DiscoveryViewMode
import com.example.bitesavers.data.model.DiscoveryCategory
import com.example.bitesavers.data.model.OfferUiModel
import com.example.bitesavers.data.model.UserRole

sealed interface DiscoveryUiEvent {
    data class OnSearchQueryChanged(val query: String) : DiscoveryUiEvent
    data class OnCategorySelected(val category: DiscoveryCategory) : DiscoveryUiEvent
    data class OnViewModeSelected(val mode: DiscoveryViewMode) : DiscoveryUiEvent
    data class OnOfferClicked(val offer: OfferUiModel) : DiscoveryUiEvent
    data class OnStoreClicked(val store: DiscoveryStoreUiModel) : DiscoveryUiEvent
    data class OnToggleBookmark(val offerId: String) : DiscoveryUiEvent
    data class OnRoleChanged(val role: UserRole) : DiscoveryUiEvent
    data object OnNotificationClicked : DiscoveryUiEvent
    data class OnMapMarkerClicked(val offerId: String?) : DiscoveryUiEvent
    data class OnMapOfferNavigate(val offerId: String) : DiscoveryUiEvent
    data object OnResetFilters : DiscoveryUiEvent
}