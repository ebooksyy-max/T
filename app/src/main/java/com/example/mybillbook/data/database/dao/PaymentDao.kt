package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC, id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE supplierId = :supplierId ORDER BY paymentDate DESC")
    fun getPaymentsForSupplier(supplierId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE paymentDate >= :start AND paymentDate <= :end ORDER BY paymentDate DESC")
    fun getPaymentsBetween(start: Long, end: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE paymentDate >= :start AND paymentDate <= :end ORDER BY paymentDate DESC")
    suspend fun getPaymentsBetweenDirect(start: Long, end: Long): List<PaymentEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE paymentDate >= :startOfDay AND paymentDate <= :endOfDay")
    fun getTodayCollections(startOfDay: Long, endOfDay: Long): Flow<Double>
}
