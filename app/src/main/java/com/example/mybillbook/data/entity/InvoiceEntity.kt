package com.example.mybillbook.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["invoiceNumber"], unique = true),
        Index(value = ["customerId"]),
        Index(value = ["invoiceDate"]),
        Index(value = ["status"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerNameSnapshot: String = "Walk-in Customer",
    val customerPhoneSnapshot: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = PaymentMethods.CASH,
    val status: String = InvoiceStatus.PAID.name,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["invoiceId"]),
        Index(value = ["productId"])
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceId: Long,
    val productId: Long? = null,
    val productNameSnapshot: String,
    val quantity: Double,
    val unit: String = "Pcs",
    val rate: Double,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val amount: Double
)
