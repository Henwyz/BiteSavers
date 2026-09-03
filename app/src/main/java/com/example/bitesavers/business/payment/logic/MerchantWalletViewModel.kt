package com.example.bitesavers.business.wallet.logic

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
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    // Loads live store balance and payout history
    fun loadWalletData(storeId: String) {
        if (storeId.isBlank()) return
        viewModelScope.launch {
            isLoading = true
            val store = repository.fetchStoreWallet(storeId)
            if (store != null) {
                balance = store.balance
            }
            payoutHistory = repository.fetchPayoutHistory(storeId)
            isLoading = false
        }
    }

    // Requests a withdrawal and updates local states
    fun requestWithdrawal(storeId: String, amount: Double, cardNumber: String, onSuccess: () -> Unit) {
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

            val result = repository.requestWithdrawal(storeId, amount, cardNumber)
            result.fold(
                onSuccess = {
                    successMessage = "Withdrawal request submitted successfully."
                    loadWalletData(storeId)
                    onSuccess()
                },
                onFailure = { error ->
                    errorMessage = error.localizedMessage ?: "Withdrawal failed."
                }
            )
            isLoading = false
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}