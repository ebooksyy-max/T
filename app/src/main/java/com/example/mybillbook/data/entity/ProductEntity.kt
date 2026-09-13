package com.example.mybillbook.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["barcode"]),
        Index(value = ["sku"]),
        Index(value = ["categoryId"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val sku: String = "",
    val barcode: String = "",
    val categoryId: Long? = null,
    val unit: String = "Pcs",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val gstRate: Double = 0.0,
    val stockQuantity: Double = 0.0,
    val minimumStock: Double = 5.0,
    val description: String = "",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
