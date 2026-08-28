package com.example.bitesavers.customer.payment.logic

import java.util.Calendar

object PaymentValidationUtils {

    /**
     * Validates 16-digit bank card number
     */
    fun isValidCardNumber(cardNumber: String): Boolean {
        val digitsOnly = cardNumber.filter { it.isDigit() }
        return digitsOnly.length == 16
    }

    /**
     * Validates card expiry in MM/YY or MMYY format against current month/year.
     */
    fun isValidExpiryDate(expiry: String): Boolean {
        val digitsOnly = expiry.filter { it.isDigit() }
        if (digitsOnly.length != 4) return false

        val inputMonth = digitsOnly.substring(0, 2).toIntOrNull() ?: return false
        val inputYear = digitsOnly.substring(2, 4).toIntOrNull() ?: return false

        if (inputMonth !in 1..12) return false

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR) % 100
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        return when {
            inputYear > currentYear -> true
            inputYear == currentYear -> inputMonth >= currentMonth
            else -> false
        }
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