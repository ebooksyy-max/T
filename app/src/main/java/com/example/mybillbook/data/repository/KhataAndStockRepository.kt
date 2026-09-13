package com.example.mybillbook.data.repository

import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.KhataTransactionEntity
import com.example.mybillbook.data.entity.KhataTransactionType
import com.example.mybillbook.data.entity.StockTransactionEntity
import kotlinx.coroutines.flow.Flow

class KhataRepository(private val database: AppDatabase) {
    private val khataDao = database.khataTransactionDao()

    fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransactionEntity>> =
        khataDao.getTransactionsForCustomer(customerId)

    suspend fun getTransactionsForCustomerDirect(customerId: Long): List<KhataTransactionEntity> =
        khataDao.getTransactionsForCustomerDirect(customerId)

    fun getTransactionsForSupplier(supplierId: Long): Flow<List<KhataTransactionEntity>> =
        khataDao.getTransactionsForSupplier(supplierId)

    suspend fun addManualEntry(
        customerId: Long?,
        supplierId: Long?,
        type: KhataTransactionType,
        amount: Double,
        note: String
    ): Long {
        return khataDao.insertTransaction(
            KhataTransactionEntity(
                customerId = customerId,
                supplierId = supplierId,
                transactionType = type.name,
                referenceType = "MANUAL",
                amount = amount,
                note = note
            )
        )
    }
}

class StockRepository(private val database: AppDatabase) {
    private val stockDao = database.stockTransactionDao()

    val allStockTransactions: Flow<List<StockTransactionEntity>> = stockDao.getAllStockTransactions()

    fun getStockHistory(productId: Long): Flow<List<StockTransactionEntity>> =
        stockDao.getTransactionsForProduct(productId)
}
