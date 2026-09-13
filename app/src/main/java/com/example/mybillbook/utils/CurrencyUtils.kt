package com.example.mybillbook.utils

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    private val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    private val numberFormat: DecimalFormat = DecimalFormat("#,##,##0.00")

    fun format(amount: Double?, symbol: String = "₹"): String {
        val value = amount ?: 0.0
        return "$symbol${numberFormat.format(value)}"
    }

    fun formatWithoutSymbol(amount: Double?): String {
        val value = amount ?: 0.0
        return numberFormat.format(value)
    }

    fun parseAmount(input: String): Double {
        if (input.isBlank()) return 0.0
        return try {
            val clean = input.replace("₹", "").replace(",", "").trim()
            clean.toDoubleOrNull() ?: 0.0
        } catch (_: Exception) {
            0.0
        }
    }
}
