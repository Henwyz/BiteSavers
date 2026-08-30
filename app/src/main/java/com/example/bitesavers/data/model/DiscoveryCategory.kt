package com.example.bitesavers.data.model

import androidx.annotation.StringRes
import com.example.bitesavers.R

enum class DiscoveryCategory {
    ALL,
    HOT_MEALS,
    BAKERY,
    DESSERTS,
    BEVERAGES,
    FREE
}

// Maps each category enum to its localized display string in strings.xml
@StringRes
fun DiscoveryCategory.getDisplayNameRes(): Int {
    return when (this) {
        DiscoveryCategory.ALL -> R.string.category_all
        DiscoveryCategory.HOT_MEALS -> R.string.category_hot_meals
        DiscoveryCategory.BAKERY -> R.string.category_bakery
        DiscoveryCategory.DESSERTS -> R.string.category_desserts
        DiscoveryCategory.BEVERAGES -> R.string.category_beverages
        DiscoveryCategory.FREE -> R.string.category_free
    }
}