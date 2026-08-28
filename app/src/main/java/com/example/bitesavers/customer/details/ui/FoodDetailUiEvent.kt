package com.example.bitesavers.customer.details.ui

sealed interface FoodDetailUiEvent {
    data object OnNavigateBack : FoodDetailUiEvent
    data object OnIncreaseQuantity : FoodDetailUiEvent
    data object OnDecreaseQuantity : FoodDetailUiEvent
    data object OnReserveClicked : FoodDetailUiEvent
    data object OnToggleBookmark : FoodDetailUiEvent
}