package com.example.mybillbook.data.repository

import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.InvoiceStatus
import com.example.mybillbook.data.entity.PaymentMethods
import java.util.Calendar

enum class ReportDateRange(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time")
}

data class SalesReportData(
    val invoiceCount: Int,
    val grossSales: Double,
    val discount: Double,
    val taxAmount: Double,
    val netSales: Double,
    val receivedAmount: Double,
    val dueAmount: Double
)

data class PurchaseReportData(
    val purchaseCount: Int,
    val totalPurchases: Double,
    val paidAmount: Double,
    val dueAmount: Double
)

data class ProfitReportData(
    val salesRevenue: Double,
    val costOfGoodsSold: Double,
    val expenses: Double,
    val grossProfit: Double
)

data class CustomerDueItem(
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val totalDue: Double,
    val oldestBillDate: Long
)

data class StockReportItem(
    val productId: Long,
    val productName: String,
    val currentStock: Double,
    val unit: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val stockValue: Double,
    val isLowStock: Boolean
)

data class PaymentModeBreakdown(
    val cash: Double,
    val upi: Double,
    val card: Double,
    val bank: Double,
    val other: Double,
    val total: Double
)

class ReportRepository(private val database: AppDatabase) {
    private val invoiceDao = database.invoiceDao()
    private val invoiceItemDao = database.invoiceItemDao()
    private val purchaseDao = database.purchaseDao()
    private val paymentDao = database.paymentDao()
    private val productDao = database.productDao()
    private val customerDao = database.customerDao()
    private val expenseDao = database.expenseDao()

    fun calculateDateRange(range: ReportDateRange): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = System.currentTimeMillis()

        return when (range) {
            ReportDateRange.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            ReportDateRange.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            ReportDateRange.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            ReportDateRange.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            ReportDateRange.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            ReportDateRange.ALL_TIME -> {
                Pair(0L, now + 86400000L)
            }
        }
    }

    suspend fun getSalesReport(start: Long, end: Long): SalesReportData {
        val invoices = invoiceDao.getInvoicesBetweenDirect(start, end)
            .filter { it.status != InvoiceStatus.CANCELLED.name }

        val gross = invoices.sumOf { it.subtotal }
        val discount = invoices.sumOf { it.discount }
        val tax = invoices.sumOf { it.taxAmount }
        val grandTotal = invoices.sumOf { it.grandTotal }
        val received = invoices.sumOf { it.receivedAmount }
        val due = invoices.sumOf { it.dueAmount }

        return SalesReportData(
            invoiceCount = invoices.size,
            grossSales = gross,
            discount = discount,
            taxAmount = tax,
            netSales = grandTotal,
            receivedAmount = received,
            dueAmount = due
        )
    }

    suspend fun getPurchaseReport(start: Long, end: Long): PurchaseReportData {
        val purchases = purchaseDao.getPurchasesBetweenDirect(start, end)
            .filter { it.status != InvoiceStatus.CANCELLED.name }

        return PurchaseReportData(
            purchaseCount = purchases.size,
            totalPurchases = purchases.sumOf { it.grandTotal },
            paidAmount = purchases.sumOf { it.paidAmount },
            dueAmount = purchases.sumOf { it.dueAmount }
        )
    }

    suspend fun getProfitReport(start: Long, end: Long): ProfitReportData {
        val invoices = invoiceDao.getInvoicesBetweenDirect(start, end)
            .filter { it.status != InvoiceStatus.CANCELLED.name }
        val salesRevenue = invoices.sumOf { it.grandTotal }

        // Calculate Cost of Goods Sold from invoice items and product purchase prices
        var cogs = 0.0
        val productCache = mutableMapOf<Long, Double>()
        for (inv in invoices) {
            val items = invoiceItemDao.getItemsForInvoiceDirect(inv.id)
            for (item in items) {
                if (item.productId != null) {
                    val purchasePrice = productCache.getOrPut(item.productId) {
                        productDao.getProductByIdDirect(item.productId)?.purchasePrice ?: 0.0
                    }
                    cogs += (purchasePrice * item.quantity)
                }
            }
        }

        val expensesList = expenseDao.getExpensesBetweenDirect(start, end)
        val totalExpenses = expensesList.sumOf { it.amount }
        val grossProfit = salesRevenue - cogs - totalExpenses

        return ProfitReportData(
            salesRevenue = salesRevenue,
            costOfGoodsSold = cogs,
            expenses = totalExpenses,
            grossProfit = grossProfit
        )
    }

    suspend fun getCustomerDueReport(): List<CustomerDueItem> {
        val customers = customerDao.getAllCustomersDirect()
        val dueList = mutableListOf<CustomerDueItem>()

        for (c in customers) {
            val invoices = invoiceDao.getInvoicesByCustomerDirect(c.id)
                .filter { it.dueAmount > 0 && it.status != InvoiceStatus.CANCELLED.name }
            val totalDue = invoices.sumOf { it.dueAmount } + c.openingBalance
            if (totalDue > 0.0) {
                val oldestDate = invoices.minOfOrNull { it.invoiceDate } ?: c.createdAt
                dueList.add(
                    CustomerDueItem(
                        customerId = c.id,
                        customerName = c.name,
                        customerPhone = c.phone,
                        totalDue = totalDue,
                        oldestBillDate = oldestDate
                    )
                )
            }
        }

        return dueList.sortedByDescending { it.totalDue }
    }

    suspend fun getStockReport(): List<StockReportItem> {
        val products = productDao.getAllProductsDirect()
        return products.map { p ->
            StockReportItem(
                productId = p.id,
                productName = p.name,
                currentStock = p.stockQuantity,
                unit = p.unit,
                purchasePrice = p.purchasePrice,
                sellingPrice = p.sellingPrice,
                stockValue = p.stockQuantity * p.purchasePrice,
                isLowStock = p.stockQuantity <= p.minimumStock
            )
        }.sortedBy { it.currentStock }
    }

    suspend fun getPaymentReport(start: Long, end: Long): PaymentModeBreakdown {
        val payments = paymentDao.getPaymentsBetweenDirect(start, end)
        var cash = 0.0
        var upi = 0.0
        var card = 0.0
        var bank = 0.0
        var other = 0.0

        for (p in payments) {
            when (p.paymentMethod) {
                PaymentMethods.CASH -> cash += p.amount
                PaymentMethods.UPI -> upi += p.amount
                PaymentMethods.CARD -> card += p.amount
                PaymentMethods.BANK -> bank += p.amount
                else -> other += p.amount
            }
        }

        return PaymentModeBreakdown(
            cash = cash,
            upi = upi,
            card = card,
            bank = bank,
            other = other,
            total = cash + upi + card + bank + other
        )
    }
}
