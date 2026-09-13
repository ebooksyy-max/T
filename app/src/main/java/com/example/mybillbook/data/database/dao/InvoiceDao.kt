package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.data.entity.InvoiceItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: Long)

    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC, id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC, id DESC")
    suspend fun getAllInvoicesDirect(): List<InvoiceEntity>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    fun getInvoiceById(id: Long): Flow<InvoiceEntity?>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceByIdDirect(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getInvoiceByNumberDirect(invoiceNumber: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY invoiceDate DESC")
    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY invoiceDate DESC")
    fun getInvoicesByCustomer(customerId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId ORDER BY invoiceDate DESC")
    suspend fun getInvoicesByCustomerDirect(customerId: Long): List<InvoiceEntity>

    @Query("SELECT * FROM invoices WHERE invoiceNumber LIKE '%' || :query || '%' OR customerNameSnapshot LIKE '%' || :query || '%' OR customerPhoneSnapshot LIKE '%' || :query || '%' ORDER BY invoiceDate DESC")
    fun searchInvoices(query: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices ORDER BY invoiceDate DESC LIMIT :limit")
    fun getRecentInvoices(limit: Int = 5): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceDate >= :start AND invoiceDate <= :end ORDER BY invoiceDate DESC")
    fun getInvoicesBetween(start: Long, end: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE invoiceDate >= :start AND invoiceDate <= :end ORDER BY invoiceDate DESC")
    suspend fun getInvoicesBetweenDirect(start: Long, end: Long): List<InvoiceEntity>

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM invoices WHERE status != 'CANCELLED'")
    fun getTotalSales(): Flow<Double>

    @Query("SELECT COALESCE(SUM(dueAmount), 0.0) FROM invoices WHERE status != 'CANCELLED'")
    fun getTotalReceivable(): Flow<Double>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM invoices WHERE invoiceDate >= :startOfDay AND invoiceDate <= :endOfDay AND status != 'CANCELLED'")
    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(receivedAmount), 0.0) FROM invoices WHERE invoiceDate >= :startOfDay AND invoiceDate <= :endOfDay AND status != 'CANCELLED'")
    fun getTodayReceivedFromInvoices(startOfDay: Long, endOfDay: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(grandTotal), 0.0) FROM invoices WHERE invoiceDate >= :startOfMonth AND invoiceDate <= :endOfMonth AND status != 'CANCELLED'")
    fun getMonthlySales(startOfMonth: Long, endOfMonth: Long): Flow<Double>

    @Query("UPDATE invoices SET status = :newStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateInvoiceStatus(id: Long, newStatus: String, updatedAt: Long = System.currentTimeMillis())
}

@Dao
interface InvoiceItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItemEntity>>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoiceDirect(invoiceId: Long): List<InvoiceItemEntity>

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteItemsByInvoiceId(invoiceId: Long)
}
