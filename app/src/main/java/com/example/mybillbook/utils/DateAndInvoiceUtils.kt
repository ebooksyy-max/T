package com.example.mybillbook.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val standardDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return standardDateFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    fun formatForFileName(timestamp: Long = System.currentTimeMillis()): String {
        return fileDateFormat.format(Date(timestamp))
    }
}

object InvoiceNumberGenerator {
    fun generateInvoiceNumber(prefix: String, sequenceNumber: Long): String {
        val cleanPrefix = prefix.ifBlank { "INV-" }
        return String.format(Locale.getDefault(), "%s%04d", cleanPrefix, sequenceNumber)
    }

    fun generatePurchaseNumber(prefix: String, sequenceNumber: Long): String {
        val cleanPrefix = prefix.ifBlank { "PUR-" }
        return String.format(Locale.getDefault(), "%s%04d", cleanPrefix, sequenceNumber)
    }
}
