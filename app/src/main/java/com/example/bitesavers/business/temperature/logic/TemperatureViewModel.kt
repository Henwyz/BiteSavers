package com.example.bitesavers.business.temperature.logic

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
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
            }
        }
    }

    // Finds the pre-inserted box by its box code where store_id is null, and assigns the store_id to claim it
    fun addNewBox(storeId: String, boxCode: String, selectedStorageType: String, onError: (Int) -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existingBoxes = SupabaseClient.client.from("storage_boxes")
                    .select {
                        filter {
                            eq("box_code", boxCode)
                        }
                    }
                    .decodeList<StorageBoxDto>()

                if (existingBoxes.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onError(R.string.error_box_not_found)
                    }
                    return@launch
                }

                // 👇 Declared targetBox here so it can be referenced below
                val targetBox = existingBoxes.first()

                if (!targetBox.storeId.isNullOrBlank() && targetBox.storeId != storeId) {
                    withContext(Dispatchers.Main) {
                        onError(R.string.error_box_claimed)
                    }
                    return@launch
                }

                if (!targetBox.storageType.equals(selectedStorageType, ignoreCase = true)) {
                    withContext(Dispatchers.Main) {
                        onError(R.string.error_box_mismatch)
                    }
                    return@launch
                }

                SupabaseClient.client.from("storage_boxes")
                    .update({
                        set("store_id", storeId)
                    }) {
                        filter {
                            eq("box_code", boxCode)
                        }
                    }

                fetchUnitsForStore(storeId)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                // Instead of deleting the row, unclaim it by setting store_id to null
                SupabaseClient.client.from("storage_boxes")
                    .update({
                        set("store_id", null as String?)
                    }) {
                        filter {
                            eq("id", boxId)
                        }
                    }

                // Refresh the list after unclaiming
                fetchUnitsForStore(storeId)

                withContext(Dispatchers.Main) {
                    // Clear or switch selected unit if the unclaimed one was selected
                    if (selectedUnit?.id == boxId) {
                        selectedUnit = units.firstOrNull()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}