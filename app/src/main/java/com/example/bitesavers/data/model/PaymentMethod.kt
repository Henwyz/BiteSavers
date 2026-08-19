package com.example.bitesavers.data.model

enum class PaymentMethod(
    val id: String,
    val displayName: String,
    val subtitle: String
) {
    BITESAVER_PAY(
        id = "BITESAVER_PAY",
        displayName = "BiteSaver Pay",
        subtitle = "Balance: RM 43.50"
    ),
    TNG_EWALLET(
        id = "TNG_EWALLET",
        displayName = "Touch 'n Go eWallet",
        subtitle = "Linked: +60 12-*** 7890"
    ),
    CASH_ON_PICKUP(
        id = "CASH_ON_PICKUP",
        displayName = "Cash on Pickup",
        subtitle = "Pay directly at store counter"
    );

    companion object {
        fun fromId(id: String): PaymentMethod {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BITESAVER_PAY
        }
    }
}