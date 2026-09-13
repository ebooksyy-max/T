package com.example.mybillbook.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "businesses")
data class BusinessEntity(
    @PrimaryKey val id: Long = 1L,
    val businessName: String = "My Business",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstin: String = "",
    val pan: String = "",
    val logoPath: String? = null,
    val upiId: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val ifsc: String = "",
    val signaturePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
