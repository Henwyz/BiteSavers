package com.example.bitesavers.business.payout.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.data.remote.UserSession
import com.example.bitesavers.data.remote.dto.MerchantPayoutDto
import com.example.bitesavers.data.repository.PayoutRepository
import kotlinx.coroutines.launch

class PayoutViewModel(
    private val repository: PayoutRepository = PayoutRepository()
) : ViewModel() {

    var walletBalance by mutableDoubleStateOf(0.0)
        private set

    var payoutHistory by mutableStateOf<List<MerchantPayoutDto>>(emptyList())
        private set

    // Credentials loaded directly from SharedPreferences for instant retrieval
    var bankName by mutableStateOf("")
    var cardNumber by mutableStateOf("")
    var cardHolder by mutableStateOf("")

    var amountInput by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessageRes by mutableStateOf<Int?>(null)
    var successMessageRes by mutableStateOf<Int?>(null)

    init {
        loadCachedCardDetails()
    }

    // Restores bank details from local device storage without network delay
    private fun loadCachedCardDetails() {
        bankName = UserSession.getPayoutCardBank()
        cardNumber = UserSession.getPayoutCardNumber()
        cardHolder = UserSession.getPayoutCardHolder()
    }

    // Fetches live user balance and remote payout history from database
    fun loadFinancialData(storeId: String) {
        val userId = UserSession.getUserId()
        if (userId.isBlank()) return

        viewModelScope.launch {
            isLoading = true
            walletBalance = repository.getUserWalletBalance(userId)
            payoutHistory = repository.getPayoutHistory(storeId)
            isLoading = false
        }
    }

    // Validates, saves credentials locally, and records transaction in Supabase
    fun submitPayout(storeId: String, onSuccess: () -> Unit = {}) {
        val userId = UserSession.getUserId()
        val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
        val cleanCard = cardNumber.filter { it.isDigit() }

        // Clear previous state messages
        errorMessageRes = null
        successMessageRes = null

        if (bankName.isBlank() || cleanCard.isBlank() || cardHolder.isBlank()) {
            errorMessageRes = R.string.payout_error_empty_fields
            return
        }
        if (cleanCard.length < 9 || cleanCard.length > 16) {
            errorMessageRes = R.string.payout_error_invalid_card
            return
        }
        if (parsedAmount <= 0.0) {
            errorMessageRes = R.string.payout_error_invalid_amount
            return
        }
        if (parsedAmount > walletBalance) {
            errorMessageRes = R.string.payout_error_insufficient_funds
            return
        }

        viewModelScope.launch {
            isLoading = true

            // 1. Cache card details into SharedPreferences for future low-latency loads
            UserSession.savePayoutCard(
                cardNumber = cleanCard,
                bankName = bankName.trim(),
                holderName = cardHolder.trim()
            )

            // 2. Transmit transaction record to Supabase and deduct user's wallet_balance
            val isSuccess = repository.requestPayout(
                userId = userId,
                storeId = storeId,
                amount = parsedAmount,
                cardNumber = cleanCard,
                currentBalance = walletBalance
            )

            isLoading = false
            if (isSuccess) {
                walletBalance -= parsedAmount
                amountInput = ""
                successMessageRes = R.string.payout_success_toast
                payoutHistory = repository.getPayoutHistory(storeId)
                onSuccess()
            } else {
                errorMessageRes = R.string.payout_error_insufficient_funds
            }
        }
    }
}