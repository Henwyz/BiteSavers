package com.example.bitesavers.customer.ticket.logic

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.customer.ticket.data.TicketUiState
import com.example.bitesavers.customer.ticket.ui.TicketUiEvent
import com.example.bitesavers.data.remote.SupabaseClient
import com.example.bitesavers.data.repository.OfferRepository
import com.example.bitesavers.data.repository.OrderRepository
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class TicketViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val offerRepository: OfferRepository = OfferRepository()
    private val orderRepository: OrderRepository = OrderRepository()
    private val client = SupabaseClient.client

    private val _uiState = MutableStateFlow(TicketUiState(isLoading = true))
    val uiState: StateFlow<TicketUiState> = _uiState.asStateFlow()

    // Tracks if review prompt has already been shown or dismissed to prevent repeating popups
    private var hasPromptedReview = false

    init {
        val orderId: String? = savedStateHandle.get<String>("orderId")
        if (!orderId.isNullOrBlank()) {
            loadTicketDetails(orderId)
            startOrderStatusObserver(orderId)
        } else {
            generateDummyTicket()
        }
    }

    fun onEvent(event: TicketUiEvent) {
        when (event) {
            is TicketUiEvent.OnBackClick -> {
                // Handled at navigation level
            }
            is TicketUiEvent.OnDismissReviewSheet -> {
                // Mark as dismissed so the observer loop does not re-open it
                hasPromptedReview = true
                _uiState.update { it.copy(showReviewSheet = false) }
            }
            is TicketUiEvent.OnSubmitReview -> {
                hasPromptedReview = true
                submitOrderReview(event.rating, event.comment)
            }
        }
    }

    private fun loadTicketDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val order = orderRepository.fetchOrderById(orderId)
            if (order != null) {
                val offer = offerRepository.fetchOfferById(order.offerId)

                val originalTotal = (offer?.originalPrice ?: 0.0) * order.quantity
                val moneySaved = (originalTotal - order.totalPrice).coerceAtLeast(0.0)
                val co2Saved = abs(0.8 * order.quantity)
                val pin = order.pickupPin ?: (((abs(orderId.hashCode()) % 9000) + 1000).toString())
                val shortOrderId = "BS-" + orderId.takeLast(5).uppercase()

                val formattedPaymentMethod = when (order.paymentMethod.uppercase()) {
                    "BITESAVER_PAY" -> "BiteSaver Pay"
                    "TNG_EWALLET", "TNG" -> "Touch 'n Go eWallet"
                    "CASH_ON_PICKUP", "CASH" -> "Cash on Pickup"
                    else -> order.paymentMethod
                }

                val isCompleted = order.status.equals("COMPLETED", ignoreCase = true)
                val alreadyReviewed = !order.remark.isNullOrBlank() && order.remark.contains("Rating:")
                val shouldShowReview = isCompleted && !alreadyReviewed && !hasPromptedReview

                if (shouldShowReview) {
                    hasPromptedReview = true
                }

                _uiState.update {
                    it.copy(
                        orderId = shortOrderId,
                        rawOrderId = orderId,
                        storeId = order.storeId,
                        storeName = offer?.storeName ?: "Store",
                        pickupWindow = "Today, until closing",
                        itemName = "${offer?.title ?: "Surprise Bag"} x${order.quantity}",
                        totalPaid = order.totalPrice,
                        paymentMethod = formattedPaymentMethod,
                        savedAmount = moneySaved,
                        co2Saved = co2Saved,
                        pin = pin,
                        orderStatus = order.status,
                        isCompleted = isCompleted,
                        isReviewSubmitted = alreadyReviewed,
                        showReviewSheet = shouldShowReview,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Continuously polls until the order is COMPLETED and updates the ticket UI
    private fun startOrderStatusObserver(orderId: String) {
        viewModelScope.launch {
            while (isActive) {
                delay(3000)

                // If already completed in UI, stop running network requests
                if (_uiState.value.isCompleted) {
                    break
                }

                val order = orderRepository.fetchOrderById(orderId)
                if (order != null) {
                    val isCompleted = order.status.equals("COMPLETED", ignoreCase = true)
                    val alreadyReviewed = !order.remark.isNullOrBlank() && order.remark.contains("Rating:")

                    // Updates ticket state and opens review sheet once
                    if (isCompleted) {
                        val triggerReview = !alreadyReviewed && !hasPromptedReview
                        if (triggerReview) {
                            hasPromptedReview = true
                        }

                        _uiState.update {
                            it.copy(
                                orderStatus = "COMPLETED",
                                isCompleted = true,
                                showReviewSheet = triggerReview
                            )
                        }
                        // Order is completed, observer can finish
                        break
                    }
                }
            }
        }
    }

    private fun submitOrderReview(rating: Int, comment: String) {
        val rawId = _uiState.value.rawOrderId
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (rawId.isNotBlank()) {
                    val reviewText = if (comment.isBlank()) {
                        "Rating: $rating/5"
                    } else {
                        "Rating: $rating/5 | $comment"
                    }

                    client.from("orders")
                        .update({
                            set("remark", reviewText)
                        }) {
                            filter { eq("id", rawId) }
                        }
                }
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            showReviewSheet = false,
                            isReviewSubmitted = true
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(showReviewSheet = false) }
                }
            }
        }
    }

    private fun generateDummyTicket() {
        _uiState.update { currentState ->
            currentState.copy(
                orderId = "BS-28401",
                storeName = "Mawar Delights Cafe",
                pickupWindow = "Today, 8:00 PM - 9:30 PM",
                itemName = "Smoked Salmon Salad Bowl x1",
                totalPaid = 11.00,
                paymentMethod = "BiteSaver Pay",
                savedAmount = 11.00,
                co2Saved = 0.8,
                pin = "7667",
                orderStatus = "COMPLETED",
                isCompleted = true,
                showReviewSheet = false,
                isLoading = false
            )
        }
    }
}