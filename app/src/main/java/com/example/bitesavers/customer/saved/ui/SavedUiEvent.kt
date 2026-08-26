package com.example.bitesavers.customer.saved.ui

sealed interface SavedUiEvent {
    data class OnOfferClicked(val offerId: String) : SavedUiEvent
    data class OnToggleBookmark(val offerId: String) : SavedUiEvent
}