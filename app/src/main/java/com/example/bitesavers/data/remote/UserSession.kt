package com.example.bitesavers.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    // Storage file name saved locally on the Android device
    private const val PREFS_NAME = "bitesaver_user_session"
    private const val KEY_USER_ID = "saved_user_id"
    private const val KEY_USER_ROLE = "saved_user_role"
    private const val KEY_STORE_STATUS = "saved_store_status" // Added to persist store status on disk
    private const val KEY_NOTIFIED_BANNER_IDS = "saved_notified_banner_ids"

    // Locally cached merchant payout credentials for low-latency retrieval
    private const val KEY_PAYOUT_CARD_NUMBER = "saved_payout_card_number"
    private const val KEY_PAYOUT_CARD_BANK = "saved_payout_card_bank"
    private const val KEY_PAYOUT_CARD_HOLDER = "saved_payout_card_holder"

    // Holds Android SharedPreferences instance once initialized
    private var sharedPreferences: SharedPreferences? = null

    // In-memory reactive state flow (keeps your existing architecture working)
    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUserRole = MutableStateFlow("CONSUMER")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    // Event broadcast flow to immediately refresh notification UI across screens
    private val _notificationRefreshEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val notificationRefreshEvent: SharedFlow<Unit> = _notificationRefreshEvent.asSharedFlow()

    private var storeStatus: String = "PENDING"

    /**
     * Initializes disk storage access and restores any previously saved session.
     * Call this once when the application or MainActivity starts.
     */
    fun init(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            // Read previously saved values from the phone's disk storage
            val savedId = sharedPreferences?.getString(KEY_USER_ID, "").orEmpty()
            val savedRole = sharedPreferences?.getString(KEY_USER_ROLE, "CONSUMER").orEmpty()
            val savedStoreStatus = sharedPreferences?.getString(KEY_STORE_STATUS, "PENDING").orEmpty() // Restores saved store status

            // Restore the values into our in-memory StateFlows and variables
            _currentUserId.value = savedId
            _currentUserRole.value = savedRole
            storeStatus = if (savedStoreStatus.isBlank()) "PENDING" else savedStoreStatus
        }
    }

    /**
     * Checks if a valid user session is stored on disk.
     */
    fun isLoggedIn(): Boolean {
        return getUserId().isNotBlank()
    }

    /**
     * Saves user login info both into memory (StateFlow) and onto phone disk storage.
     */
    fun saveSession(userId: String, role: String) {
        _currentUserId.value = userId
        _currentUserRole.value = role

        // Persist to phone disk
        sharedPreferences?.edit()
            ?.putString(KEY_USER_ID, userId)
            ?.putString(KEY_USER_ROLE, role)
            ?.commit()
    }

    // Retain original method so existing repositories continue working seamlessly
    fun setUserId(userId: String) {
        _currentUserId.value = userId
        sharedPreferences?.edit()?.putString(KEY_USER_ID, userId)?.commit()
    }

    fun getUserId(): String = _currentUserId.value

    fun getUserRole(): String = _currentUserRole.value

    /**
     * Caches the merchant's payout account details directly to SharedPreferences.
     * This avoids remote network roundtrips when loading the payout interface.
     */
    fun savePayoutCard(cardNumber: String, bankName: String, holderName: String) {
        sharedPreferences?.edit()
            ?.putString(KEY_PAYOUT_CARD_NUMBER, cardNumber)
            ?.putString(KEY_PAYOUT_CARD_BANK, bankName)
            ?.putString(KEY_PAYOUT_CARD_HOLDER, holderName)
            ?.commit()
    }

    fun getPayoutCardNumber(): String = sharedPreferences?.getString(KEY_PAYOUT_CARD_NUMBER, "").orEmpty()

    fun getPayoutCardBank(): String = sharedPreferences?.getString(KEY_PAYOUT_CARD_BANK, "").orEmpty()

    fun getPayoutCardHolder(): String = sharedPreferences?.getString(KEY_PAYOUT_CARD_HOLDER, "").orEmpty()

    /**
     * Returns the set of order IDs that have already displayed a system heads-up banner.
     */
    fun getNotifiedBannerOrderIds(): Set<String> {
        val prefs = sharedPreferences ?: return emptySet()
        val saved = prefs.getStringSet(KEY_NOTIFIED_BANNER_IDS, emptySet()) ?: emptySet()
        return HashSet(saved)
    }

    /**
     * Persists order IDs that have already fired a banner so they don't spam the user.
     */
    fun markBannerAsShown(orderId: String) {
        val prefs = sharedPreferences ?: return
        val updatedSet = HashSet(getNotifiedBannerOrderIds())
        updatedSet.add(orderId)
        prefs.edit()?.putStringSet(KEY_NOTIFIED_BANNER_IDS, updatedSet)?.commit()
    }

    /**
     * Triggers active ViewModels across the app to reload notifications immediately.
     */
    fun notifyNewOrderUpdate() {
        _notificationRefreshEvent.tryEmit(Unit)
    }

    /**
     * Clears user data from both in-memory StateFlow and device storage on logout.
     */
    fun clear() {
        _currentUserId.value = ""
        _currentUserRole.value = "CONSUMER"
        storeStatus = "PENDING" // Reset store status on logout

        // Wipe the saved keys from the device
        sharedPreferences?.edit()
            ?.clear()
            ?.commit()
    }

    fun setStoreStatus(status: String) {
        storeStatus = status
        sharedPreferences?.edit()?.putString(KEY_STORE_STATUS, status)?.commit() // Persists status change to disk
    }

    fun getStoreStatus(): String {
        return storeStatus
    }
}