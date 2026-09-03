package com.example.bitesavers.business.payment.data

import com.example.bitesavers.data.remote.dto.MerchantPayoutDto

object MerchantWalletData {
    // Mock balance
    var currentBalance: Double = 350.00

    // Mock payout history list
    val mockPayouts = mutableListOf(
        MerchantPayoutDto(
            id = "p_01",
            storeId = "store_test_123",
            amount = 120.50,
            cardNumber = "4242",
            status = "COMPLETED",
            createdAt = "2026-09-02 10:00"
        ),
        MerchantPayoutDto(
            id = "p_02",
            storeId = "store_test_123",
            amount = 75.00,
            cardNumber = "1234",
            status = "PENDING",
            createdAt = "2026-09-04 14:20"
        )
    )
}