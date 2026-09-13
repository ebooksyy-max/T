package com.example.mybillbook.data.repository

import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.*
import com.example.mybillbook.domain.model.PurchaseWithItems
import kotlinx.coroutines.flow.Flow

class PurchaseRepository(private val database: AppDatabase) {
    private val purchaseDao = database.purchaseDao()
    private val purchaseItemDao = database.purchaseItemDao()
    private val paymentDao = database.paymentDao()
    private val khataDao = database.khataTransactionDao()
    private val stockDao = database.stockTransactionDao()
    private val productDao = database.productDao()
    private val settingsDao = database.settingsDao()

    val allPurchases: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
    val totalPayable: Flow<Double> = purchaseDao.getTotalPayable()
    val totalPurchases: Flow<Double> = purchaseDao.getTotalPurchases()

    fun searchPurchases(query: String): Flow<List<PurchaseEntity>> = purchaseDao.searchPurchases(query)

    fun getPurchaseById(id: Long): Flow<PurchaseEntity?> = purchaseDao.getPurchaseById(id)

    suspend fun getPurchaseWithItems(id: Long): PurchaseWithItems? {
        val purchase = purchaseDao.getPurchaseByIdDirect(id) ?: return null
        val items = purchaseItemDao.getItemsForPurchaseDirect(id)
        return PurchaseWithItems(purchase = purchase, items = items)
    }

    suspend fun savePurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>
    ): Long = saveFinalizedPurchase(purchase, items)

    suspend fun saveFinalizedPurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>
    ): Long = database.withTransaction {
        // 1. Save purchase
        val purchaseId = purchaseDao.insertPurchase(purchase)

        // 2. Save purchase items
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        purchaseItemDao.insertItems(itemsWithId)

        // 3 & 4. Increase product stock and create stock transactions
        for (item in itemsWithId) {
            if (item.productId != null) {
                val product = productDao.getProductByIdDirect(item.productId)
                if (product != null) {
                    val newStock = product.stockQuantity + item.quantity
                    productDao.updateStock(product.id, newStock)

                    stockDao.insertTransaction(
                        StockTransactionEntity(
                            productId = product.id,
                            transactionType = StockTransactionType.PURCHASE.name,
                            quantity = item.quantity,
                            referenceId = purchaseId,
                            note = "Purchase ${purchase.purchaseNumber}",
                            transactionDate = purchase.purchaseDate
                        )
                    )
                }
            }
        }

        // 5. Create supplier Khata transaction
        if (purchase.supplierId != null) {
            khataDao.insertTransaction(
                KhataTransactionEntity(
                    supplierId = purchase.supplierId,
                    transactionType = KhataTransactionType.DEBIT.name,
                    referenceId = purchaseId,
                    referenceType = "PURCHASE",
                    amount = purchase.grandTotal,
                    transactionDate = purchase.purchaseDate,
                    note = "Purchase ${purchase.purchaseNumber}"
                )
            )

            if (purchase.paidAmount > 0.0) {
                khataDao.insertTransaction(
                    KhataTransactionEntity(
                        supplierId = purchase.supplierId,
                        transactionType = KhataTransactionType.PAYMENT.name,
                        referenceId = purchaseId,
                        referenceType = "PURCHASE_PAYMENT",
                        amount = purchase.paidAmount,
                        transactionDate = purchase.purchaseDate,
                        note = "Paid for Purchase ${purchase.purchaseNumber}"
                    )
                )
            }
        }

        // 6. Create payment if paid
        if (purchase.paidAmount > 0.0) {
            paymentDao.insertPayment(
                PaymentEntity(
                    supplierId = purchase.supplierId,
                    purchaseId = purchaseId,
                    amount = purchase.paidAmount,
                    paymentMethod = purchase.paymentMethod,
                    paymentDate = purchase.purchaseDate,
                    note = "Payment for Purchase ${purchase.purchaseNumber}"
                )
            )
        }

        settingsDao.incrementPurchaseNumber()
        purchaseId
    }
}
