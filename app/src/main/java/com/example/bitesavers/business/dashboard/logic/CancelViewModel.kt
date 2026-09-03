package com.example.bitesavers.business.dashboard.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.business.dashboard.data.CheckOrderData
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.NotificationDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserWalletDto(
    val id: String,
    @SerialName("wallet_balance")
    val walletBalance: Double = 0.0
)

// DTO to fetch owner_id from stores table
@Serializable
data class StoreOwnerDto(
    val id: String,
    @SerialName("owner_id")
    val ownerId: String
)
class CancelViewModel : ViewModel() {
    var orderData by mutableStateOf<CheckOrderData?>(null)
        private set

    var selectedReason by mutableStateOf("")
    var otherReasonText by mutableStateOf("")
    var refundMethod by mutableStateOf("")

    var errorMessageRes by mutableStateOf<Int?>(null)
    var generalErrorText by mutableStateOf<String?>(null)

    var isSubmitting by mutableStateOf(false)
        private set
    var isSuccessDialogOpen by mutableStateOf(false)

    fun loadOrder(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client.from("orders").select(
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, offers(*)")
                ) {
                    filter {
                        eq("id", orderId)
                    }
                }.decodeList<CheckOrderData>()

                val fetched = result.firstOrNull()
                withContext(Dispatchers.Main) {
                    orderData = fetched
                    refundMethod = fetched?.paymentMethod.orEmpty()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    generalErrorText = "Failed to load order: ${e.message}"
                }
            }
        }
    }

    fun submitCancellation(onSuccess: () -> Unit) {
        val currentOrder = orderData ?: return
        errorMessageRes = null
        generalErrorText = null

        val finalReason = if (selectedReason == "Other") {
            if (otherReasonText.trim().isBlank()) {
                errorMessageRes = R.string.other_reason_empty_error
                return
            }
            otherReasonText.trim().take(1000)
        } else {
            if (selectedReason.isBlank()) {
                errorMessageRes = R.string.select_reason_error
                return
            }
            selectedReason
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isSubmitting = true }
            try {
                // Process Refund based on Payment Method
                val isBiteSaverPay = currentOrder.paymentMethod.equals("BITESAVER_PAY", ignoreCase = true)
                val isTngPay = currentOrder.paymentMethod.contains("TNG", ignoreCase = true)
                val isCardPay = currentOrder.paymentMethod.contains("CARD", ignoreCase = true)

                val refundMessageSuffix = when {
                    isBiteSaverPay -> "RM %.2f has been refunded to your BiteSavers wallet.".format(currentOrder.totalPrice)
                    isTngPay -> "RM %.2f will be refunded to your Touch 'n Go eWallet within 1-3 working days.".format(currentOrder.totalPrice)
                    isCardPay -> "RM %.2f will be refunded to your card within 3-5 working days.".format(currentOrder.totalPrice)
                    else -> "Refund of RM %.2f has been recorded.".format(currentOrder.totalPrice)
                }

                if (isBiteSaverPay) {
                    // Check merchant balance first before modifying any funds
                    var storeOwnerId: String? = null
                    if (currentOrder.storeId.isNotBlank()) {
                        val storeResult = SupabaseClient.client.from("stores").select {
                            filter { eq("id", currentOrder.storeId) }
                        }.decodeList<StoreOwnerDto>()
                        storeOwnerId = storeResult.firstOrNull()?.ownerId
                    }

                    if (storeOwnerId.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            errorMessageRes = null
                            generalErrorText = "Store merchant account not found."
                            isSubmitting = false
                        }
                        return@launch
                    }

                    val merchantList = SupabaseClient.client.from("users").select {
                        filter { eq("id", storeOwnerId) }
                    }.decodeList<UserWalletDto>()
                    val merchant = merchantList.firstOrNull()

                    // Guard check: Halt cancellation if merchant has insufficient balance
                    if (merchant == null || merchant.walletBalance < currentOrder.totalPrice) {
                        withContext(Dispatchers.Main) {
                            errorMessageRes = R.string.error_merchant_insufficient_balance
                            isSubmitting = false
                        }
                        return@launch
                    }

                    //Deduct from merchant balance
                    val newMerchantBalance = merchant.walletBalance - currentOrder.totalPrice
                    SupabaseClient.client.from("users").update({
                        set("wallet_balance", newMerchantBalance)
                    }) {
                        filter { eq("id", merchant.id) }
                    }

                    // Fetch and update customer wallet balance
                    val customerList = SupabaseClient.client.from("users").select {
                        filter { eq("id", currentOrder.userId) }
                    }.decodeList<UserWalletDto>()

                    val customer = customerList.firstOrNull()
                    if (customer != null) {
                        val newCustomerBalance = customer.walletBalance + currentOrder.totalPrice
                        SupabaseClient.client.from("users").update({
                            set("wallet_balance", newCustomerBalance)
                        }) {
                            filter { eq("id", customer.id) }
                        }
                    }

                    if (currentOrder.storeId.isNotBlank()) {
                        // Find the owner of this store
                        val storeResult = SupabaseClient.client.from("stores").select {
                            filter { eq("id", currentOrder.storeId) }
                        }.decodeList<StoreOwnerDto>()

                        val storeOwnerId = storeResult.firstOrNull()?.ownerId
                        if (!storeOwnerId.isNullOrBlank()) {
                            // Find merchant user record and deduct balance
                            val merchantList = SupabaseClient.client.from("users").select {
                                filter { eq("id", storeOwnerId) }
                            }.decodeList<UserWalletDto>()

                            val merchant = merchantList.firstOrNull()
                            if (merchant != null) {
                                val newMerchantBalance = (merchant.walletBalance - currentOrder.totalPrice).coerceAtLeast(0.0)
                                SupabaseClient.client.from("users").update({
                                    set("wallet_balance", newMerchantBalance)
                                }) {
                                    filter { eq("id", merchant.id) }
                                }
                            }
                        }
                    }
                }

                // 2. Update orders table status & cancel reason
                SupabaseClient.client.from("orders").update({
                    set("status", "CANCELLED")
                }) {
                    filter { eq("id", currentOrder.id) }
                }

                // 3. Insert notification for target customer
                val notifId = "notif_${UUID.randomUUID().toString().take(8)}"
                val notification = NotificationDto(
                    id = notifId,
                    userId = currentOrder.userId,
                    orderId = currentOrder.id,
                    title = "Order Cancelled ⚠️",
                    message = "Your order ${currentOrder.shortOrderId} was cancelled. Reason: $finalReason. $refundMessageSuffix",
                    isRead = false
                )
                SupabaseClient.client.from("user_notifications").insert(notification)

                // 4. Trigger success pop-out on Main thread
                withContext(Dispatchers.Main) {
                    isSuccessDialogOpen = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    generalErrorText = "Error: ${e.localizedMessage ?: "Failed to process cancellation"}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSubmitting = false
                }
            }
        }
    }
}