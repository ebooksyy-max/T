package com.example.mybillbook.data.repository

import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.*
import com.example.mybillbook.domain.model.InvoiceWithItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InvoiceRepository(private val database: AppDatabase) {
    private val invoiceDao = database.invoiceDao()
    private val invoiceItemDao = database.invoiceItemDao()
    private val paymentDao = database.paymentDao()
    private val khataDao = database.khataTransactionDao()
    private val stockDao = database.stockTransactionDao()
    private val productDao = database.productDao()
    private val settingsDao = database.settingsDao()

    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val totalSales: Flow<Double> = invoiceDao.getTotalSales()
    val totalReceivable: Flow<Double> = invoiceDao.getTotalReceivable()

    suspend fun getAllInvoicesDirect(): List<InvoiceEntity> = invoiceDao.getAllInvoicesDirect()

    fun getRecentInvoices(limit: Int = 5): Flow<List<InvoiceEntity>> = invoiceDao.getRecentInvoices(limit)

    fun searchInvoices(query: String): Flow<List<InvoiceEntity>> = invoiceDao.searchInvoices(query)

    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesByStatus(status)

    fun getInvoiceById(id: Long): Flow<InvoiceEntity?> = invoiceDao.getInvoiceById(id)

    suspend fun getInvoiceByIdDirect(id: Long): InvoiceEntity? = invoiceDao.getInvoiceByIdDirect(id)

    suspend fun getInvoiceWithItems(id: Long): InvoiceWithItems? {
        val invoice = invoiceDao.getInvoiceByIdDirect(id) ?: return null
        val items = invoiceItemDao.getItemsForInvoiceDirect(id)
        return InvoiceWithItems(invoice = invoice, items = items)
    }

    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<Double> =
        invoiceDao.getTodaySales(startOfDay, endOfDay)

    fun getTodayCollection(startOfDay: Long, endOfDay: Long): Flow<Double> =
        paymentDao.getTodayCollections(startOfDay, endOfDay)

    fun getMonthlySales(startOfMonth: Long, endOfMonth: Long): Flow<Double> =
        invoiceDao.getMonthlySales(startOfMonth, endOfMonth)

    /**
     * Atomically saves an invoice and executes the 8-step business transaction
     */
    suspend fun saveFinalizedInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long = database.withTransaction {
        // 1. Insert InvoiceEntity
        val invoiceId = invoiceDao.insertInvoice(invoice)

        // 2. Insert InvoiceItemEntity records with proper invoiceId
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceItemDao.insertItems(itemsWithId)

        // 3. Insert PaymentEntity if payment > 0
        if (invoice.receivedAmount > 0.0) {
            paymentDao.insertPayment(
                PaymentEntity(
                    customerId = invoice.customerId,
                    invoiceId = invoiceId,
                    amount = invoice.receivedAmount,
                    paymentMethod = invoice.paymentMethod,
                    paymentDate = invoice.invoiceDate,
                    note = "Payment for ${invoice.invoiceNumber}"
                )
            )
        }

        // 4. Insert KhataTransactionEntity if customer exists and has credit or payment
        if (invoice.customerId != null) {
            // Record debit (sales bill amount)
            khataDao.insertTransaction(
                KhataTransactionEntity(
                    customerId = invoice.customerId,
                    transactionType = KhataTransactionType.CREDIT.name,
                    referenceId = invoiceId,
                    referenceType = "INVOICE",
                    amount = invoice.grandTotal,
                    transactionDate = invoice.invoiceDate,
                    note = "Invoice ${invoice.invoiceNumber}"
                )
            )

            // Record payment in Khata if received > 0
            if (invoice.receivedAmount > 0.0) {
                khataDao.insertTransaction(
                    KhataTransactionEntity(
                        customerId = invoice.customerId,
                        transactionType = KhataTransactionType.PAYMENT.name,
                        referenceId = invoiceId,
                        referenceType = "INVOICE_PAYMENT",
                        amount = invoice.receivedAmount,
                        transactionDate = invoice.invoiceDate,
                        note = "Payment for ${invoice.invoiceNumber}"
                    )
                )
            }
        }

        // 5 & 6. Stock deduction & StockTransactionEntity records
        for (item in itemsWithId) {
            if (item.productId != null) {
                val product = productDao.getProductByIdDirect(item.productId)
                if (product != null) {
                    val newStock = (product.stockQuantity - item.quantity).coerceAtLeast(0.0)
                    productDao.updateStock(product.id, newStock)

                    stockDao.insertTransaction(
                        StockTransactionEntity(
                            productId = product.id,
                            transactionType = StockTransactionType.SALE.name,
                            quantity = item.quantity,
                            referenceId = invoiceId,
                            note = "Sale in ${invoice.invoiceNumber}",
                            transactionDate = invoice.invoiceDate
                        )
                    )
                }
            }
        }

        // 7. Increment invoice number counter
        settingsDao.incrementInvoiceNumber()

        // 8. Transaction committed successfully
        invoiceId
    }

    suspend fun saveDraftInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long = database.withTransaction {
        val invoiceId = invoiceDao.insertInvoice(invoice.copy(status = InvoiceStatus.DRAFT.name))
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        invoiceItemDao.insertItems(itemsWithId)
        invoiceId
    }

    suspend fun cancelInvoice(invoiceId: Long, reason: String = "Cancelled by user") = database.withTransaction {
        val invoice = invoiceDao.getInvoiceByIdDirect(invoiceId) ?: return@withTransaction
        if (invoice.status == InvoiceStatus.CANCELLED.name) return@withTransaction

        // Reverse stock if it was not draft
        if (invoice.status != InvoiceStatus.DRAFT.name) {
            val items = invoiceItemDao.getItemsForInvoiceDirect(invoiceId)
            for (item in items) {
                if (item.productId != null) {
                    productDao.incrementStock(item.productId, item.quantity)
                    stockDao.insertTransaction(
                        StockTransactionEntity(
                            productId = item.productId,
                            transactionType = StockTransactionType.RETURN.name,
                            quantity = item.quantity,
                            referenceId = invoiceId,
                            note = "Return: Invoice ${invoice.invoiceNumber} Cancelled"
                        )
                    )
                }
            }

            // Reverse Khata if customer was linked
            if (invoice.customerId != null) {
                khataDao.insertTransaction(
                    KhataTransactionEntity(
                        customerId = invoice.customerId,
                        transactionType = KhataTransactionType.DEBIT.name,
                        referenceId = invoiceId,
                        referenceType = "INVOICE_CANCEL",
                        amount = invoice.dueAmount,
                        note = "Cancelled Invoice ${invoice.invoiceNumber}"
                    )
                )
            }
        }

        invoiceDao.updateInvoiceStatus(invoiceId, InvoiceStatus.CANCELLED.name)
    }

    suspend fun deleteDraftInvoice(invoiceId: Long) = database.withTransaction {
        val invoice = invoiceDao.getInvoiceByIdDirect(invoiceId) ?: return@withTransaction
        if (invoice.status == InvoiceStatus.DRAFT.name) {
            invoiceItemDao.deleteItemsByInvoiceId(invoiceId)
            invoiceDao.deleteInvoiceById(invoiceId)
        }
    }
}
