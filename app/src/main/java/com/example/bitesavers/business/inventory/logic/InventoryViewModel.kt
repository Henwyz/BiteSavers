package com.example.bitesavers.business.inventory.logic

import androidx.lifecycle.ViewModel
import com.example.bitesavers.business.inventory.data.ListingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class InventoryViewModel : ViewModel() {
    private val _listings = MutableStateFlow<List<ListingItem>>(
        listOf(
            ListingItem("1", "Butter Croissant", "Bakery", 4.50, 1.50, 8, "06:00 PM", "Active"),
            ListingItem("2", "Blueberry Muffin", "Bakery", 5.00, 2.00, 5, "05:30 PM", "Active"),
            ListingItem("3", "Sourdough Loaf", "Bakery", 12.00, 5.00, 2, "06:00 PM", "Paused"),
            ListingItem("4", "Banana Bread Slice", "Bakery", 10.00, 3.00, 5, "05.45 PM", "Sold out")
        )
    )
    val listings: StateFlow<List<ListingItem>> = _listings.asStateFlow()

    fun deleteListing(id: String) {
        _listings.value = _listings.value.filter { it.id != id }
    }
}