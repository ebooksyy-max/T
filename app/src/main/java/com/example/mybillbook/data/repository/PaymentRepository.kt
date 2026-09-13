package com.example.mybillbook.data.repository

import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.InvoiceStatus
import com.example.mybillbook.data.entity.KhataTransactionEntity
import com.example.mybillbook.data.entity.KhataTransactionType
import com.example.mybillbook.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

class PaymentRepository(private val database: AppDatabase) {
    private val paymentDao = database.paymentDao()
    private val khataDao = database.khataTransactionDao()
    private val invoiceDao = database.invoiceDao()

    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForCustomer(customerId)

    fun getPaymentsForSupplier(supplierId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForSupplier(supplierId)

    suspend fun recordCustomerPayment(
        customerId: Long,
        amount: Double,
        paymentMethod: String,
        note: String,
        invoiceId: Long? = null,
        paymentDate: Long = System.currentTimeMillis()
    ): Long = database.withTransaction {
        // 1. Insert Payment
        val paymentId = paymentDao.insertPayment(
            PaymentEntity(
                customerId = customerId,
                invoiceId = invoiceId,
                amount = amount,
                paymentMethod = paymentMethod,
                paymentDate = paymentDate,
                note = note
            )
        )

        // 2. Insert Khata payment entry
        khataDao.insertTransaction(
            KhataTransactionEntity(
                customerId = customerId,
                transactionType = KhataTransactionType.PAYMENT.name,
                referenceId = paymentId,
                referenceType = "MANUAL_PAYMENT",
                amount = amount,
                transactionDate = paymentDate,
                note = note.ifBlank { "Payment Received" }
            )
        )

        // 3. If tied to specific invoice, update that invoice's received and due amount
        if (invoiceId != null) {
            val invoice = invoiceDao.getInvoiceByIdDirect(invoiceId)
            if (invoice != null) {
                val newReceived = invoice.receivedAmount + amount
                val newDue = (invoice.grandTotal - newReceived).coerceAtLeast(0.0)
                val newStatus = if (newDue == 0.0) InvoiceStatus.PAID.name else InvoiceStatus.PARTIALLY_PAID.name
                invoiceDao.updateInvoice(
                    invoice.copy(
                        receivedAmount = newReceived,
                        dueAmount = newDue,
                        status = newStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        paymentId
    }

    suspend fun recordSupplierPayment(
        supplierId: Long,
        amount: Double,
        paymentMethod: String,
        note: String,
        purchaseId: Long? = null,
        paymentDate: Long = System.currentTimeMillis()
    ): Long = database.withTransaction {
        val paymentId = paymentDao.insertPayment(
            PaymentEntity(
                supplierId = supplierId,
                purchaseId = purchaseId,
                amount = amount,
                paymentMethod = paymentMethod,
                paymentDate = paymentDate,
                note = note
            )
        )

        khataDao.insertTransaction(
            KhataTransactionEntity(
                supplierId = supplierId,
                transactionType = KhataTransactionType.PAYMENT.name,
                referenceId = paymentId,
                referenceType = "SUPPLIER_PAYMENT",
                amount = amount,
                transactionDate = paymentDate,
                note = note.ifBlank { "Payment Paid to Supplier" }
            )
        )

        paymentId
    }
}
