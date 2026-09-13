package com.example.mybillbook.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    indices = [
        Index(value = ["purchaseNumber"], unique = true),
        Index(value = ["supplierId"]),
        Index(value = ["purchaseDate"]),
        Index(value = ["status"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val purchaseNumber: String,
    val supplierId: Long? = null,
    val supplierNameSnapshot: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueAmount: Double = 0.0,
    val paymentMethod: String = PaymentMethods.CASH,
    val status: String = InvoiceStatus.PAID.name,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"]),
        Index(value = ["productId"])
    ]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val purchaseId: Long,
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
