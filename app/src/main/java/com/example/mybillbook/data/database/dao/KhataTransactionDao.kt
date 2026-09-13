package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.KhataTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: KhataTransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(transaction: KhataTransactionEntity)

    @Query("SELECT * FROM khata_transactions WHERE customerId = :customerId ORDER BY transactionDate DESC, id DESC")
    fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransactionEntity>>

    @Query("SELECT * FROM khata_transactions WHERE customerId = :customerId ORDER BY transactionDate DESC, id DESC")
    suspend fun getTransactionsForCustomerDirect(customerId: Long): List<KhataTransactionEntity>

    @Query("SELECT * FROM khata_transactions WHERE supplierId = :supplierId ORDER BY transactionDate DESC, id DESC")
    fun getTransactionsForSupplier(supplierId: Long): Flow<List<KhataTransactionEntity>>

    @Query("SELECT * FROM khata_transactions WHERE supplierId = :supplierId ORDER BY transactionDate DESC, id DESC")
    suspend fun getTransactionsForSupplierDirect(supplierId: Long): List<KhataTransactionEntity>

    @Query("SELECT * FROM khata_transactions ORDER BY transactionDate DESC")
    fun getAllTransactions(): Flow<List<KhataTransactionEntity>>

    @Query("SELECT * FROM khata_transactions WHERE transactionDate >= :start AND transactionDate <= :end ORDER BY transactionDate DESC")
    suspend fun getTransactionsBetweenDirect(start: Long, end: Long): List<KhataTransactionEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM khata_transactions WHERE customerId = :customerId AND transactionType = 'CREDIT'")
    suspend fun getCustomerTotalCredit(customerId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM khata_transactions WHERE customerId = :customerId AND transactionType = 'DEBIT'")
    suspend fun getCustomerTotalDebit(customerId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM khata_transactions WHERE customerId = :customerId AND transactionType = 'PAYMENT'")
    suspend fun getCustomerTotalPayment(customerId: Long): Double
}
