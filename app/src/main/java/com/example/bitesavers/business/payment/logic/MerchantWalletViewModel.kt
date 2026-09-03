package com.example.bitesavers.business.payment.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.data.remote.dto.MerchantPayoutDto
import com.example.bitesavers.data.repository.MerchantWalletRepository
import kotlinx.coroutines.launch

class MerchantWalletViewModel : ViewModel() {

    private val repository = MerchantWalletRepository()

    var balance by mutableStateOf(0.0)
        private set

    var payoutHistory by mutableStateOf<List<MerchantPayoutDto>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun loadWalletData(storeId: String) {
        if (storeId.isBlank()) return // Prevent querying with empty ID on initial composition

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch store balance directly from Supabase
                val store = repository.fetchStoreWallet(storeId)

                // Explicitly assign balance (even if 0.0) so it doesn't hold stale state
                balance = store?.balance ?: 0.0

                // Fetch saved payout history from Supabase
                payoutHistory = repository.fetchPayoutHistory(storeId)
            } catch (e: Exception) {
                errorMessage = "Failed to load wallet data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun requestWithdrawal(
        storeId: String,
        amount: Double,
        cardNumber: String,
        expiryInput: String,
        cvvInput: String,
        onSuccess: () -> Unit
    ) {
        if (amount <= 0.0) {
            errorMessage = "Please enter a valid amount."
            return
        }
        if (cardNumber.isBlank()) {
            errorMessage = "Please enter a valid card number."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            // Execute withdrawal and save transaction/balance update to Supabase via repository
            val result = repository.requestWithdrawal(storeId, amount, cardNumber)

            if (result.isSuccess) {
                // Refresh balance and payout list directly from Supabase
                val store = repository.fetchStoreWallet(storeId)
                balance = store?.balance ?: 0.0
                payoutHistory = repository.fetchPayoutHistory(storeId)

                successMessage = "Withdrawal request submitted successfully."
                onSuccess()
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Insufficient balance for withdrawal."
            }
            isLoading = false
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}