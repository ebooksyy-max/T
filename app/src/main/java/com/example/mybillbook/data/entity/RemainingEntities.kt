package com.example.mybillbook.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["supplierId"]),
        Index(value = ["invoiceId"]),
        Index(value = ["paymentDate"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val invoiceId: Long? = null,
    val purchaseId: Long? = null,
    val amount: Double,
    val paymentMethod: String = PaymentMethods.CASH,
    val paymentDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "khata_transactions",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["supplierId"]),
        Index(value = ["transactionDate"]),
        Index(value = ["transactionType"])
    ]
)
data class KhataTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val customerId: Long? = null,
    val supplierId: Long? = null,
    val transactionType: String, // CREDIT, DEBIT, PAYMENT
    val referenceId: Long? = null,
    val referenceType: String = "", // INVOICE, PURCHASE, MANUAL, PAYMENT
    val amount: Double,
    val transactionDate: Long = System.currentTimeMillis(),
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_transactions",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["transactionDate"]),
        Index(value = ["transactionType"])
    ]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val productId: Long,
    val transactionType: String, // SALE, PURCHASE, ADJUSTMENT_IN, ADJUSTMENT_OUT, RETURN
    val quantity: Double,
    val referenceId: Long? = null,
    val note: String = "",
    val transactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["expenseDate"]),
        Index(value = ["category"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String,
    val amount: Double,
    val expenseDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = PaymentMethods.CASH,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Long = 1L,
    val currency: String = "INR",
    val currencySymbol: String = "₹",
    val language: String = "English",
    val theme: String = "SYSTEM",
    val lowStockAlert: Boolean = true,
    val paymentReminderDays: Int = 7,
    val firstLaunchCompleted: Boolean = false
)

@Entity(tableName = "invoice_settings")
data class InvoiceSettingsEntity(
    @PrimaryKey val id: Long = 1L,
    val invoicePrefix: String = "INV-",
    val nextInvoiceNumber: Long = 1L,
    val purchasePrefix: String = "PUR-",
    val nextPurchaseNumber: Long = 1L,
    val defaultTaxRate: Double = 18.0,
    val taxInclusive: Boolean = false,
    val defaultPaymentMethod: String = PaymentMethods.CASH,
    val template: String = "STANDARD",
    val showLogo: Boolean = true,
    val showGstin: Boolean = true,
    val showSignature: Boolean = true,
    val showUpiQr: Boolean = true,
    val roundOffTotal: Boolean = true,
    val footerText: String = "Thank you for your business!",
    val termsAndConditions: String = "1. Goods once sold will not be taken back.\n2. Subject to local jurisdiction."
)
