package com.example.bitesavers.business.dashboard.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.business.dashboard.data.CheckOrderData
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.remote.dto.NotificationDto
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class VerificationViewModel : ViewModel() {
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
                    columns = io.github.jan.supabase.postgrest.query.Columns.raw("*, offers(*)")
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
                // Update order status to COMPLETED in Supabase
                SupabaseClient.client.from("orders").update(
                    {
                        set("status", "COMPLETED")
                    }
                ) {
                    filter {
                        eq("id", currentOrder.id)
                    }
                }

                //Insert notification for customer
                val notifId = "notif_${UUID.randomUUID().toString().take(8)}"
                val notification = NotificationDto(
                    id = notifId,
                    userId = currentOrder.userId,
                    orderId = currentOrder.id,
                    title = "Order Completed! 🎉",
                    message = "Order ${currentOrder.shortOrderId} has been picked up from the store.",
                    isRead = false
                )
                SupabaseClient.client.from("user_notifications").insert(notification)

                //Update local state
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