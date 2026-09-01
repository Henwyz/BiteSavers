package com.example.bitesavers.customer.store.ui

import com.example.bitesavers.data.model.OfferUiModel

// Sealed interface defining all user actions on the Store Detail screen
sealed interface StoreDetailUiEvent {
    data class LoadStore(val storeId: String) : StoreDetailUiEvent
    data class OnOfferClicked(val offer: OfferUiModel) : StoreDetailUiEvent
    data class OnCallClicked(val phoneNumber: String?) : StoreDetailUiEvent
    data class OnWhatsAppClicked(val phoneNumber: String?) : StoreDetailUiEvent
    data class OnToggleBookmark(val offerId: String) : StoreDetailUiEvent
    object OnBackClicked : StoreDetailUiEvent
}