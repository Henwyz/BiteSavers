package com.example.bitesavers.business.temperature.data

data class TemperatureData(
    val id: String,
    val name: String,
    val type: String, // "Cold Storage" or "Warm Storage"
    val temperature: String,
    val status: String,
    val isWarning: Boolean
)


