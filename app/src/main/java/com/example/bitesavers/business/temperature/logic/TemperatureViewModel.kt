package com.example.bitesavers.business.temperature.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bitesavers.R
import com.example.bitesavers.data.dto.StorageBoxDto
import com.example.bitesavers.data.repository.StorageBoxRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemperatureViewModel(
    private val storageBoxRepository: StorageBoxRepository = StorageBoxRepository()
) : ViewModel() {

    var units by mutableStateOf<List<StorageBoxDto>>(emptyList())
        private set

    var selectedUnit by mutableStateOf<StorageBoxDto?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    // Holds the polling coroutine reference
    private var pollingJob: Job? = null

    // Starts background polling loop every 5 seconds for live UI reading updates
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

    // Internal fetch implementation delegating directly to StorageBoxRepository
    private suspend fun fetchUnitsInternal(storeId: String, showLoading: Boolean) {
        if (showLoading) {
            withContext(Dispatchers.Main) { isLoading = true }
        }

        try {
            val response = storageBoxRepository.fetchBoxesByStoreId(storeId)

            withContext(Dispatchers.Main) {
                units = response
                if (selectedUnit == null && response.isNotEmpty()) {
                    selectedUnit = response.first()
                } else if (response.isNotEmpty()) {
                    selectedUnit = response.firstOrNull { it.id == selectedUnit?.id } ?: response.first()
                } else {
                    selectedUnit = null
                }
            }
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

    // Assigns an unassigned physical IoT sensor to the store using StorageBoxRepository
    fun addNewBox(
        storeId: String,
        sensorCodeInput: String,
        isHotBox: Boolean,
        onError: (Int) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = storageBoxRepository.claimBox(
                storeId = storeId,
                sensorCodeInput = sensorCodeInput,
                isHotBox = isHotBox
            )

            result.fold(
                onSuccess = {
                    fetchUnitsInternal(storeId, showLoading = false)
                    withContext(Dispatchers.Main) { onSuccess() }
                },
                onFailure = { error ->
                    withContext(Dispatchers.Main) {
                        when (error) {
                            is NoSuchElementException -> onError(R.string.error_box_not_found)
                            is IllegalStateException -> onError(R.string.error_box_claimed)
                            else -> onError(R.string.error_general)
                        }
                    }
                }
            )
        }
    }

    fun selectUnit(unit: StorageBoxDto) {
        selectedUnit = unit
    }

    fun deleteBox(boxId: String, storeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = storageBoxRepository.unassignBox(boxId)
            if (success) {
                fetchUnitsInternal(storeId, showLoading = false)
                withContext(Dispatchers.Main) {
                    if (selectedUnit?.id == boxId) {
                        selectedUnit = units.firstOrNull()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}