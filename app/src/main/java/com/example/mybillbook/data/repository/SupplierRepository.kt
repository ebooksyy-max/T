package com.example.mybillbook.data.repository

import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.KhataTransactionEntity
import com.example.mybillbook.data.entity.KhataTransactionType
import com.example.mybillbook.data.entity.SupplierEntity
import com.example.mybillbook.domain.model.SupplierBalanceSummary
import kotlinx.coroutines.flow.Flow

class SupplierRepository(private val database: AppDatabase) {
    private val supplierDao = database.supplierDao()
    private val khataDao = database.khataTransactionDao()
    private val purchaseDao = database.purchaseDao()

    val allSuppliers: Flow<List<SupplierEntity>> = supplierDao.getAllSuppliers()

    fun searchSuppliers(query: String): Flow<List<SupplierEntity>> = supplierDao.searchSuppliers(query)

    fun getSupplierById(id: Long): Flow<SupplierEntity?> = supplierDao.getSupplierById(id)

    suspend fun getSupplierByIdDirect(id: Long): SupplierEntity? = supplierDao.getSupplierByIdDirect(id)

    suspend fun insertSupplier(supplier: SupplierEntity): Long = database.withTransaction {
        val id = supplierDao.insertSupplier(supplier)
        if (supplier.openingBalance > 0.0) {
            khataDao.insertTransaction(
                KhataTransactionEntity(
                    supplierId = id,
                    transactionType = KhataTransactionType.DEBIT.name, // We owe supplier
                    referenceType = "OPENING_BALANCE",
                    amount = supplier.openingBalance,
                    note = "Opening Balance Payable"
                )
            )
        }
        id
    }

    suspend fun updateSupplier(supplier: SupplierEntity) {
        supplierDao.updateSupplier(supplier.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteSupplier(id: Long) {
        supplierDao.deleteSupplierById(id)
    }

    suspend fun getSupplierBalanceSummary(supplierId: Long): SupplierBalanceSummary {
        val purchases = purchaseDao.getPurchasesBySupplierDirect(supplierId)
        val totalPurchases = purchases.sumOf { it.grandTotal }
        val totalPaid = purchases.sumOf { it.paidAmount }
        val netPayable = purchases.sumOf { it.dueAmount }

        return SupplierBalanceSummary(
            supplierId = supplierId,
            totalPurchases = totalPurchases,
            totalPaid = totalPaid,
            netPayable = netPayable
        )
    }
}
