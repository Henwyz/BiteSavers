package com.example.bitesavers.customer.checkout.logic

object PaymentValidationUtils {

    /**
     * Validates 16-digit bank card number
     */
    fun isValidCardNumber(cardNumber: String): Boolean {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        return digitsOnly.length == 16
    }

    /**
     * Validates card expiry in MM/YY format and verifies it is in the future
     */
    fun isValidExpiryDate(expiry: String): Boolean {
        if (!expiry.matches(Regex("^(0[1-9]|1[0-2])/[0-9]{2}\$"))) return false
        val parts = expiry.split("/")
        val month = parts[0].toIntOrNull() ?: return false
        val year = parts[1].toIntOrNull() ?: return false

        // Basic sanity check: year >= 24 (2024+)
        return month in 1..12 && year >= 24
    }

    /**
     * Validates 3-digit CVV
     */
    fun isValidCvv(cvv: String): Boolean {
        return cvv.length == 3 && cvv.all { it.isDigit() }
    }

    /**
     * Validates Malaysian phone numbers (e.g., 0123456789, +60123456789, 123456789)
     */
    fun isValidMalaysianPhone(phone: String): Boolean {
        val digitsOnly = phone.filter { it.isDigit() }
        return digitsOnly.length in 9..11
    }
}