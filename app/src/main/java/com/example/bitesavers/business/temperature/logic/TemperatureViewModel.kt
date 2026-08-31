package com.example.bitesavers.business.temperature.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bitesavers.business.temperature.data.TemperatureData
import com.example.bitesavers.business.temperature.data.TemperatureRepository

class TemperatureViewModel : ViewModel() {
    private val repository = TemperatureRepository()

    var units by mutableStateOf<List<TemperatureData>>(emptyList())
        private set

    // Track the currently selected unit for the box
    var selectedUnit by mutableStateOf<TemperatureData?>(null)
        private set

    init {
        loadTemperatureData()
    }

    fun loadTemperatureData() {
        units = repository.getConnectedUnits()
        if (units.isNotEmpty()) {
            selectedUnit = units[0] // set default to Refrigerator A
        }
    }

    // Function called when a user taps a unit card
    fun selectUnit(unit: TemperatureData) {
        selectedUnit = unit
    }
}