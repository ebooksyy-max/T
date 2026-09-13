package com.example.mybillbook.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"])
    ]
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val companyName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstin: String = "",
    val openingBalance: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
