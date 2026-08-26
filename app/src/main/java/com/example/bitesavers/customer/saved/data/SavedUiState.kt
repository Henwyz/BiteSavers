package com.example.bitesavers.customer.saved.data

import com.example.bitesavers.data.model.OfferUiModel

data class SavedUiState(
    val isLoading: Boolean = true,
    val savedOffers: List<OfferUiModel> = emptyList()
)