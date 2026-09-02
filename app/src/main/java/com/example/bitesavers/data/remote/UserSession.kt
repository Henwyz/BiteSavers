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
    private const val KEY_READ_NOTIFICATION_IDS = "saved_read_notification_ids"
    private const val KEY_NOTIFIED_BANNER_IDS = "saved_notified_banner_ids"
    private const val KEY_CLEARED_NOTIFICATION_IDS = "saved_cleared_notification_ids"

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

            // Restore the values into our in-memory StateFlows
            _currentUserId.value = savedId
            _currentUserRole.value = savedRole
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
            ?.apply()
    }

    // Retain original method so existing repositories continue working seamlessly
    fun setUserId(userId: String) {
        _currentUserId.value = userId
        sharedPreferences?.edit()?.putString(KEY_USER_ID, userId)?.apply()
    }

    fun getUserId(): String = _currentUserId.value

    fun getUserRole(): String = _currentUserRole.value

    /**
     * Returns the set of order IDs that have already displayed a system heads-up banner.
     */
    fun getNotifiedBannerOrderIds(): Set<String> {
        val prefs = sharedPreferences ?: return emptySet()
        return prefs.getStringSet(KEY_NOTIFIED_BANNER_IDS, emptySet()) ?: emptySet()
    }

    /**
     * Persists order IDs that have already fired a banner so they don't spam the user.
     */
    fun markBannerAsShown(orderId: String) {
        val prefs = sharedPreferences ?: return
        val currentSet = getNotifiedBannerOrderIds().toMutableSet()
        currentSet.add(orderId)
        prefs.edit()?.putStringSet(KEY_NOTIFIED_BANNER_IDS, currentSet)?.apply()
    }

    /**
     * Returns the set of notification order IDs that the user has already opened and read in the dialog.
     */
    fun getReadNotificationIds(): Set<String> {
        val prefs = sharedPreferences ?: return emptySet()
        return prefs.getStringSet(KEY_READ_NOTIFICATION_IDS, emptySet()) ?: emptySet()
    }

    /**
     * Persists read notification IDs to disk so the counter remains accurate across screen transitions.
     */
    fun markNotificationIdsAsRead(newIds: Set<String>) {
        val prefs = sharedPreferences ?: return
        val currentSet = getReadNotificationIds().toMutableSet()
        currentSet.addAll(newIds)
        prefs.edit()?.putStringSet(KEY_READ_NOTIFICATION_IDS, currentSet)?.apply()
    }

    /**
     * Returns IDs of notifications that the user explicitly chose to clear/dismiss.
     */
    fun getClearedNotificationIds(): Set<String> {
        val prefs = sharedPreferences ?: return emptySet()
        return prefs.getStringSet(KEY_CLEARED_NOTIFICATION_IDS, emptySet()) ?: emptySet()
    }

    /**
     * Persists dismissed notification IDs so they no longer appear in the dialog list.
     */
    fun clearAllNotifications(idsToClear: Set<String>) {
        val prefs = sharedPreferences ?: return
        val currentSet = getClearedNotificationIds().toMutableSet()
        currentSet.addAll(idsToClear)
        prefs.edit()?.putStringSet(KEY_CLEARED_NOTIFICATION_IDS, currentSet)?.apply()
        // Also mark them read
        markNotificationIdsAsRead(idsToClear)
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

        // Wipe the saved keys from the device
        sharedPreferences?.edit()
            ?.clear()
            ?.apply()
    }
}