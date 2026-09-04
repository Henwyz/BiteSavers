package com.example.bitesavers.util

object CardMaskUtils {
    // Formats raw digits into a masked preview pattern: •••• •••• •••• 1234
    fun maskCardNumber(rawNumber: String?): String {
        if (rawNumber.isNullOrBlank()) return ""
        val digitsOnly = rawNumber.filter { it.isDigit() }
        return if (digitsOnly.length >= 4) {
            "•••• •••• •••• " + digitsOnly.takeLast(4)
        } else {
            "•••• $digitsOnly"
        }
    }
}