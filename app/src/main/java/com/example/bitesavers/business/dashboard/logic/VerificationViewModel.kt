package com.example.bitesavers.business.dashboard.logic

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.dashboard.data.CheckOrderData
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.NgoApplicationDto
import com.example.bitesavers.data.remote.dto.NotificationDto
import com.example.bitesavers.data.remote.dto.UserDto
import com.example.bitesavers.data.repository.MerchantWalletRepository
import com.example.bitesavers.util.GmailSender
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class VerificationViewModel : ViewModel() {
    private val walletRepository = MerchantWalletRepository()

    var orderData by mutableStateOf<CheckOrderData?>(null)
        private set

    var enteredPin by mutableStateOf("")
    var pinError by mutableStateOf<String?>(null)
    var isVerifying by mutableStateOf(false)
        private set
    var isSuccessDialogOpen by mutableStateOf(false)
    var isDeleted by mutableStateOf(false)
        private set

    // Fetch full order data joined with offers table
    fun loadOrder(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = SupabaseClient.client.from("orders").select(
                    columns = Columns.raw("*, offers(*)")
                ) {
                    filter {
                        eq("id", orderId)
                    }
                }.decodeList<CheckOrderData>()

                orderData = result.firstOrNull()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Verify customer pickup pin and update status to COMPLETED
    fun completeOrderVerification(onSuccess: () -> Unit) {
        val currentOrder = orderData ?: return
        pinError = null

        // 1. Guard check: verify entered PIN against database pickup_pin
        val expectedPin = currentOrder.pickupPin ?: ""
        if (enteredPin.trim() != expectedPin.trim()) {
            pinError = "PIN does not match customer's order."
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isVerifying = true
            try {
                // 2. Update order status to COMPLETED in Supabase
                SupabaseClient.client.from("orders").update(
                    {
                        set("status", "COMPLETED")
                    }
                ) {
                    filter {
                        eq("id", currentOrder.id)
                    }
                }

                // 2. Insert in-app notification for customer
                val notifId = "notif_${UUID.randomUUID().toString().take(8)}"
                val notification = NotificationDto(
                    id = notifId,
                    userId = currentOrder.userId,
                    orderId = currentOrder.id,
                    title = "Order Completed! 🎉",
                    message = "Order ${currentOrder.shortOrderId} has been picked up from the store.",
                    isRead = false
                )
                try {
                    SupabaseClient.client.from("user_notifications").insert(notification)
                } catch (e: Exception) {
                    Log.w("VerificationViewModel", "Could not insert notification: ${e.message}")
                }

                // 3. NGO AUTOMATED EMAIL TRIGGER
                // If this is a free NGO claim, fetch the NGO contact email and send confirmation
                if (currentOrder.isNgoFreeClaim) {
                    sendAutomatedNgoEmail(currentOrder)
                }

                // 5. Update local state
                orderData = currentOrder.copy(status = "COMPLETED")
                isSuccessDialogOpen = true
            } catch (e: Exception) {
                e.printStackTrace()
                pinError = "Failed to update order: ${e.message}"
            } finally {
                isVerifying = false
            }
        }
    }

    private suspend fun sendAutomatedNgoEmail(order: CheckOrderData) {
        // 1. Guard check: Only proceed if this order was placed as a free NGO claim
        if (!order.isNgoFreeClaim) {
            Log.d("VerificationViewModel", "Regular order. Skipping NGO email.")
            return
        }

        try {
            // 2. Strictly check if the user is an APPROVED active NGO
            val approvedApplication = SupabaseClient.client.from("ngo_applications")
                .select {
                    filter {
                        eq("user_id", order.userId)
                        eq("status", "APPROVED") // 👈 Must be APPROVED in Supabase
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<NgoApplicationDto>()
                .firstOrNull()

            if (approvedApplication == null) {
                Log.w("VerificationViewModel", "No active APPROVED NGO record found for user ${order.userId}. Skipping email.")
                return
            }

            val recipientEmail = approvedApplication.contactEmail
            val orgName = approvedApplication.organizationName

            // 3. Trigger email to the active NGO's registered contact email
            if (!recipientEmail.isNullOrBlank()) {
                Log.d("VerificationViewModel", "Sending automated email to active NGO: $recipientEmail")
                GmailSender.sendNgoClaimConfirmation(
                    recipientEmail = recipientEmail,
                    ngoOrgName = orgName,
                    orderId = order.shortOrderId,
                    itemName = order.displayItemName.ifBlank { "Surprise Surplus Meal" },
                    quantity = order.quantity
                )
            }
        } catch (e: Exception) {
            Log.e("VerificationViewModel", "Error verifying active NGO status for email", e)
        }
    }

    // Permanently remove completed order from supabase
    fun deleteOrder(orderId: String, onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("orders").delete {
                    filter { eq("id", orderId) }
                }
                withContext(Dispatchers.Main) {
                    onDeleted()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}