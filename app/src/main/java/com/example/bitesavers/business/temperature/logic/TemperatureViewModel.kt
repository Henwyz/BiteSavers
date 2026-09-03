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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemperatureViewModel : ViewModel() {

    var units by mutableStateOf<List<StorageBoxDto>>(emptyList())
        private set

    var selectedUnit by mutableStateOf<StorageBoxDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun fetchUnitsForStore(storeId: String) {
        if (storeId.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                val response = SupabaseClient.client.from("storage_boxes")
                    .select {
                        filter {
                            eq("store_id", storeId)
                        }
                    }
                    .decodeList<StorageBoxDto>()

                withContext(Dispatchers.Main) {
                    units = response
                    if (selectedUnit == null && response.isNotEmpty()) {
                        selectedUnit = response.first()
                    } else if (response.isEmpty()) {
                        selectedUnit = null
                    }
                }
            } catch (e: Exception) {
                Log.e("TemperatureVM", "fetchUnitsForStore error: ${e.message}", e)
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
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
                // Fetch candidate matching by exact box_code or case-insensitive ilike
                var candidates = SupabaseClient.client.from("storage_boxes")
                    .select {
                        filter {
                            ilike("box_code", cleanCode)
                        }
                    }
                    .decodeList<StorageBoxDto>()

                // Fallback check if user entered internal ID directly
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

                // Check if already claimed by another store
                if (!targetBox.storeId.isNullOrBlank() && targetBox.storeId != storeId) {
                    withContext(Dispatchers.Main) {
                        onError(R.string.error_box_claimed)
                    }
                    return@launch
                }

                val chosenType = if (isHotBox) "Hot Box" else "Refrigerator"
                val targetTemp = if (isHotBox) 60.0 else 4.0

                // Link to current store, set target operational temperature profile
                SupabaseClient.client.from("storage_boxes")
                    .update({
                        set("store_id", storeId)
                        set("storage_type", chosenType)
                        set("target_temperature", targetTemp)
                    }) {
                        filter {
                            eq("id", targetBox.id)
                        }
                    }

                fetchUnitsForStore(storeId)

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
                // Unclaim sensor by resetting store_id to null
                SupabaseClient.client.from("storage_boxes")
                    .update({
                        set("store_id", null as String?)
                    }) {
                        filter {
                            eq("id", boxId)
                        }
                    }

                fetchUnitsForStore(storeId)

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
}