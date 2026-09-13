package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.data.entity.StockTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransactionEntity): Long

    @Query("SELECT * FROM stock_transactions WHERE productId = :productId ORDER BY transactionDate DESC, id DESC")
    fun getTransactionsForProduct(productId: Long): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions ORDER BY transactionDate DESC, id DESC")
    fun getAllStockTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions WHERE transactionDate >= :start AND transactionDate <= :end ORDER BY transactionDate DESC")
    suspend fun getStockTransactionsBetweenDirect(start: Long, end: Long): List<StockTransactionEntity>
}

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses WHERE id = 1 LIMIT 1")
    fun getBusiness(): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses WHERE id = 1 LIMIT 1")
    suspend fun getBusinessDirect(): BusinessEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(business: BusinessEntity)
}
