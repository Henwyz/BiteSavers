package com.example.bitesavers.customer.store.data

import com.example.bitesavers.data.model.OfferUiModel

// Represents the UI state of the Store Detail screen
data class StoreDetailUiState(
    val store: StoreDetailUiModel? = null,
    val offers: List<OfferUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)