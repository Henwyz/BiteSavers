package com.example.bitesavers.data.model

import androidx.annotation.StringRes
import com.example.bitesavers.R

// Represents lifecycle state of an order with localized string resource binding
enum class OrderStatus(val dbValue: String, @StringRes val labelRes: Int) {
    PENDING("PENDING", R.string.order_status_pending),
    CONFIRMED("CONFIRMED", R.string.order_status_confirmed),
    READY_FOR_PICKUP("READY_FOR_PICKUP", R.string.order_status_ready_for_pickup),
    COMPLETED("COMPLETED", R.string.order_status_completed),
    CANCELLED("CANCELLED", R.string.order_status_cancelled),
    FAILED("FAILED", R.string.order_status_failed);

    companion object {
        // Maps database status strings to corresponding OrderStatus enum instance
        fun fromDb(value: String?): OrderStatus {
            return entries.firstOrNull { it.dbValue.equals(value, ignoreCase = true) }
                ?: PENDING
        }
    }
}