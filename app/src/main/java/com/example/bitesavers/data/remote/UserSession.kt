package com.example.bitesavers.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSession {
    // Default starting user ID in Supabase
    private val _currentUserId = MutableStateFlow("u1")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun getUserId(): String = _currentUserId.value
}