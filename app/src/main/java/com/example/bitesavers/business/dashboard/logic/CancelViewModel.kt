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
                // 1. Process Refund based on Payment Method
                val isBiteSaverPay = currentOrder.paymentMethod.equals("BITESAVER_PAY", ignoreCase = true)
                var refundMessageSuffix = "Refund of RM %.2f has been processed.".format(currentOrder.totalPrice)

                if (isBiteSaverPay) {
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

                    refundMessageSuffix = "RM %.2f has been refunded to your BiteSavers wallet.".format(currentOrder.totalPrice)
                } else if (currentOrder.paymentMethod.contains("CASH", ignoreCase = true)) {
                    refundMessageSuffix = "Please collect your cash refund of RM %.2f manually if paid.".format(currentOrder.totalPrice)
                } else {
                    refundMessageSuffix = "RM %.2f has been marked as refunded to your card.".format(currentOrder.totalPrice)
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