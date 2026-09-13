package com.example.mybillbook.domain.model

import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.data.entity.InvoiceItemEntity
import com.example.mybillbook.data.entity.InvoiceStatus
import com.example.mybillbook.data.entity.PurchaseEntity
import com.example.mybillbook.data.entity.PurchaseItemEntity

data class BillItemInput(
    val productId: Long? = null,
    val productName: String,
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val rate: Double = 0.0,
    val discount: Double = 0.0,
    val taxRate: Double = 0.0,
    val isTaxInclusive: Boolean = false,
    val currentStock: Double = 0.0
)

data class CalculatedItem(
    val productId: Long?,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val rate: Double,
    val discount: Double,
    val taxRate: Double,
    val baseAmount: Double,
    val taxAmount: Double,
    val totalAmount: Double
)

data class BillCalculation(
    val items: List<CalculatedItem>,
    val subtotal: Double,
    val billDiscount: Double,
    val totalDiscount: Double,
    val taxableAmount: Double,
    val taxAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val isInterState: Boolean,
    val grandTotal: Double,
    val receivedAmount: Double,
    val dueAmount: Double,
    val status: InvoiceStatus
)

data class InvoiceWithItems(
    val invoice: InvoiceEntity,
    val items: List<InvoiceItemEntity>
)

data class PurchaseWithItems(
    val purchase: PurchaseEntity,
    val items: List<PurchaseItemEntity>
)

data class CustomerBalanceSummary(
    val customerId: Long,
    val totalCredit: Double,
    val totalDebit: Double,
    val totalPayment: Double,
    val netBalance: Double // > 0 means customer owes us money (due), < 0 means advance
)

data class SupplierBalanceSummary(
    val supplierId: Long,
    val totalPurchases: Double,
    val totalPaid: Double,
    val netPayable: Double
)
