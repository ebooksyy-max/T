package com.example.mybillbook.data.entity

enum class InvoiceStatus {
    DRAFT,
    PAID,
    PARTIALLY_PAID,
    UNPAID,
    CANCELLED
}

enum class KhataTransactionType {
    CREDIT,
    DEBIT,
    PAYMENT
}

enum class StockTransactionType {
    SALE,
    PURCHASE,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    RETURN
}

object PaymentMethods {
    const val CASH = "Cash"
    const val UPI = "UPI / QR"
    const val CARD = "Card"
    const val BANK = "Bank Transfer"
    const val CHEQUE = "Cheque"
    const val OTHER = "Other"

    val all = listOf(CASH, UPI, CARD, BANK, CHEQUE, OTHER)
}

object AppUnits {
    val all = listOf("Pcs", "Kg", "Gm", "Ltr", "Ml", "Box", "Pack", "Mtr", "Ft", "Set", "Unit")
}

object GstRates {
    val rates = listOf(0.0, 5.0, 12.0, 18.0, 28.0)
}
