package com.example.bitesavers.business.temperature.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

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

                units = response
                if (selectedUnit == null && response.isNotEmpty()) {
                    selectedUnit = response.first()
                } else if (response.isEmpty()) {
                    selectedUnit = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    fun addNewBox(storeId: String, boxCode: String, storageType: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Determine the appropriate default/target temperature based on storage type
                val defaultTemp = if (storageType.equals("Refrigerator", ignoreCase = true)) 4.0 else 60.0

                val newBox = StorageBoxDto(
                    id = UUID.randomUUID().toString(),
                    storeId = storeId,
                    boxCode = boxCode,
                    storageType = storageType,
                    targetTemperature = defaultTemp,
                    currentTemperature = defaultTemp,
                    isLocked = true,
                    lastSyncedAt = null
                )

                SupabaseClient.client.from("storage_boxes").insert(newBox)

                // Refresh the units list to display the newly added box instantly
                fetchUnitsForStore(storeId)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
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
                    .delete {
                        filter {
                            eq("id", boxId)
                        }
                    }

                // Refresh the list after deletion
                fetchUnitsForStore(storeId)

                // Clear or switch selected unit if the deleted one was selected
                if (selectedUnit?.id == boxId) {
                    selectedUnit = units.firstOrNull()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}