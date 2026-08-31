package com.example.bitesavers.business.temperature.data

data class TemperatureData(
    val id: String,
    val name: String,
    val type: String, // "Cold Storage" or "Warm Storage"
    val temperature: String,
    val status: String,
    val isWarning: Boolean
)

class TemperatureRepository {
    fun getConnectedUnits(): List<TemperatureData> {
        return listOf(
            TemperatureData("1", "Refrigerator A", "Cold Storage", "5.8°C", "Safe", false),
            TemperatureData("2", "Refrigerator B", "Cold Storage", "7.2°C", "Safe", false),
            TemperatureData("3", "Hot Box A", "Warm Storage", "60.2°C", "Safe", false),
            TemperatureData("4", "Hot Box B", "Warm Storage", "22.6°C", "Monitor", true)
        )
    }
}
