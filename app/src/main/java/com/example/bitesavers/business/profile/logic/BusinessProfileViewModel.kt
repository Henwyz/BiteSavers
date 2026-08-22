package com.example.bitesavers.business.profile.logic

import androidx.lifecycle.ViewModel
import com.example.bitesavers.business.profile.data.BusinessProfileUiModel
import com.example.bitesavers.business.profile.data.OperatingHoursRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BusinessProfileViewModel : ViewModel() {

    private val _profile = MutableStateFlow(
        BusinessProfileUiModel(
            businessName = "Uncle Ong Bakery",
            verificationId = "DCM-2506744-B",
            isVerified = true,
            rating = 4.5,
            reviewCount = 115,
            address = "No 15, Jalan Mengkuang, 12500 KL",
            phone = "+60 3-1950 5239",
            category = "Bakery - Café",
            operatingHours = listOf(
                OperatingHoursRow("Mon - Thu", "8:30 AM - 9:00 PM"),
                OperatingHoursRow("Friday", "8:30 AM - 9:00 PM"),
                OperatingHoursRow("Sat - Sun", "9:00 AM - 10:00 PM")
            )
        )
    )
    val profile: StateFlow<BusinessProfileUiModel> = _profile.asStateFlow()
}
