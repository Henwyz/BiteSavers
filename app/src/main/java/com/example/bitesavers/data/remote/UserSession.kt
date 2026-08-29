package com.example.bitesavers.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    // Tracks the authenticated user's Supabase UUID (empty when logged out)
    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun getUserId(): String = _currentUserId.value

    // Clears the session on sign out
    fun clear() {
        _currentUserId.value = ""
    }
}