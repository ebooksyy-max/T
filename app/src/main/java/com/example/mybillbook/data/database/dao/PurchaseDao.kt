package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.PurchaseEntity
import com.example.mybillbook.data.entity.PurchaseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Long)

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC, id DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC, id DESC")
    suspend fun getAllPurchasesDirect(): List<PurchaseEntity>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    fun getPurchaseById(id: Long): Flow<PurchaseEntity?>

    @Query("SELECT * FROM purchases WHERE id = :id LIMIT 1")
    suspend fun getPurchaseByIdDirect(id: Long): PurchaseEntity?

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    suspend fun getPurchasesBySupplierDirect(supplierId: Long): List<PurchaseEntity>

    @Query("SELECT * FROM purchases WHERE purchaseNumber LIKE '%' || :query || '%' OR supplierNameSnapshot LIKE '%' || :query || '%' ORDER BY purchaseDate DESC")
    fun searchPurchases(query: String): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE purchaseDate >= :start AND purchaseDate <= :end ORDER BY purchaseDate DESC")
    fun getPurchasesBetween(start: Long, end: Long): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE purchaseDate >= :start AND purchaseDate <= :end ORDER BY purchaseDate DESC")
    suspend fun getPurchasesBetweenDirect(start: Long, end: Long): List<PurchaseEntity>

    @Query("SELECT COUNT(*) FROM purchases")
    fun getPurchaseCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(dueAmount), 0.0) FROM purchases WHERE status != 'CANCELLED'")
    fun getTotalPayable(): Flow<Double>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM purchases WHERE status != 'CANCELLED'")
    fun getTotalPurchases(): Flow<Double>
}

@Dao
interface PurchaseItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItemEntity>)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItemEntity>>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getItemsForPurchaseDirect(purchaseId: Long): List<PurchaseItemEntity>

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteItemsByPurchaseId(purchaseId: Long)
}
