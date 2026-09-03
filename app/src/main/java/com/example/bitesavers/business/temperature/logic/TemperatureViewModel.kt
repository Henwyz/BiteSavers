package com.example.bitesavers.business.temperature.logic

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemperatureViewModel : ViewModel() {

    var units by mutableStateOf<List<StorageBoxDto>>(emptyList())
        private set

    var selectedUnit by mutableStateOf<StorageBoxDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Holds the polling coroutine reference
    private var pollingJob: Job? = null

    // Starts background polling loop every 5 seconds for live sensor updates
    fun startPolling(storeId: String, intervalMillis: Long = 5000L) {
        if (storeId.isBlank()) return

        // Prevent duplicate background loops
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            // First run displays the initial loading indicator
            fetchUnitsInternal(storeId, showLoading = true)

            while (isActive) {
                delay(intervalMillis)
                // Subsequent ticks refresh data silently without full-screen loading spinner
                fetchUnitsInternal(storeId, showLoading = false)
            }
        }
    }

    // Cancels polling when screen is paused or disposed
    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // Determines if a unit's live reading breaches safe holding parameters
    private fun isTemperatureWarning(box: StorageBoxDto): Boolean {
        val isHotBox = box.storageType.equals("Hot Box", ignoreCase = true)
        return if (isHotBox) {
            val minSafeHot = (box.targetTemperature ?: 60.0) - 5.0
            box.currentTemperature < minSafeHot
        } else {
            val maxSafeCold = (box.targetTemperature ?: 4.0) + 4.0
            box.currentTemperature > maxSafeCold
        }
    }

    // Evaluates food safety condition and locks breached boxes in Supabase
    private suspend fun enforceFoodSafetyLocks(fetchedUnits: List<StorageBoxDto>): List<StorageBoxDto> {
        return fetchedUnits.map { box ->
            val warning = isTemperatureWarning(box)
            if (warning && !box.isLocked) {
                try {
                    SupabaseClient.client.from("storage_boxes")
                        .update({
                            set("is_locked", true)
                        }) {
                            filter {
                                eq("id", box.id)
                            }
                        }
                    box.copy(isLocked = true)
                } catch (e: Exception) {
                    Log.e("TemperatureVM", "Failed to auto-lock breached box ${box.id}: ${e.message}")
                    box
                }
            } else {
                box
            }
        }
    }

    // Internal fetch implementation with optional spinner toggle
    private suspend fun fetchUnitsInternal(storeId: String, showLoading: Boolean) {
        if (showLoading) {
            withContext(Dispatchers.Main) { isLoading = true }
        }

        try {
            val response = SupabaseClient.client.from("storage_boxes")
                .select {
                    filter {
                        eq("store_id", storeId)
                    }
                }
                .decodeList<StorageBoxDto>()

            val evaluatedUnits = enforceFoodSafetyLocks(response)

            withContext(Dispatchers.Main) {
                units = evaluatedUnits
                if (selectedUnit == null && evaluatedUnits.isNotEmpty()) {
                    selectedUnit = evaluatedUnits.first()
                } else if (evaluatedUnits.isNotEmpty()) {
                    selectedUnit = evaluatedUnits.firstOrNull { it.id == selectedUnit?.id } ?: evaluatedUnits.first()
                } else {
                    selectedUnit = null
                }
            }
        } catch (e: Exception) {
            Log.e("TemperatureVM", "fetchUnits error: ${e.message}", e)
        } finally {
            if (showLoading) {
                withContext(Dispatchers.Main) { isLoading = false }
            }
        }
    }

    // Direct fetch wrapper
    fun fetchUnitsForStore(storeId: String) {
        startPolling(storeId)
    }

    // Assigns an unassigned physical IoT sensor to the store with the selected target mode and profile temperature
    fun addNewBox(
        storeId: String,
        sensorCodeInput: String,
        isHotBox: Boolean,
        onError: (Int) -> Unit,
        onSuccess: () -> Unit
    ) {
        val cleanCode = sensorCodeInput.trim()
        if (cleanCode.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                var candidates = SupabaseClient.client.from("storage_boxes")
                    .select {
                        filter {
                            ilike("box_code", cleanCode)
                        }
                    }
                    .decodeList<StorageBoxDto>()

                if (candidates.isEmpty()) {
                    candidates = SupabaseClient.client.from("storage_boxes")
                        .select {
                            filter {
                                eq("id", cleanCode)
                            }
                        }
                        .decodeList<StorageBoxDto>()
                }

                if (candidates.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError(R.string.error_box_not_found)
                    }
                    return@launch
                }

                val targetBox = candidates.first()

                if (!targetBox.storeId.isNullOrBlank() && targetBox.storeId != storeId) {
                    withContext(Dispatchers.Main) {
                        onError(R.string.error_box_claimed)
                    }
                    return@launch
                }

                val chosenType = if (isHotBox) "Hot Box" else "Refrigerator"
                val targetTemp = if (isHotBox) 60.0 else 4.0

                SupabaseClient.client.from("storage_boxes")
                    .update({
                        set("store_id", storeId)
                        set("storage_type", chosenType)
                        set("target_temperature", targetTemp)
                        set("is_locked", true)
                    }) {
                        filter {
                            eq("id", targetBox.id)
                        }
                    }

                fetchUnitsInternal(storeId, showLoading = false)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("TemperatureVM", "addNewBox failed: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError(R.string.error_general)
                }
            }
        }
    }

    fun selectUnit(unit: StorageBoxDto) {
        selectedUnit = unit
    }

    fun deleteBox(boxId: String, storeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                SupabaseClient.client.from("storage_boxes")
                    .update({
                        set("store_id", null as String?)
                    }) {
                        filter {
                            eq("id", boxId)
                        }
                    }

                fetchUnitsInternal(storeId, showLoading = false)

                withContext(Dispatchers.Main) {
                    if (selectedUnit?.id == boxId) {
                        selectedUnit = units.firstOrNull()
                    }
                }
            } catch (e: Exception) {
                Log.e("TemperatureVM", "deleteBox failed: ${e.message}", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}