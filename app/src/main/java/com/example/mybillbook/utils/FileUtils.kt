package com.example.mybillbook.utils

import android.content.Context
import com.example.mybillbook.data.entity.*
import java.io.File
import java.io.FileWriter

object FileUtils {

    fun exportCustomersCsv(context: Context, customers: List<CustomerEntity>): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "Customers_${DateUtils.formatForFileName()}.csv")
        FileWriter(file).use { writer ->
            writer.appendLine("Name,Phone,Email,Address,City,GSTIN,Opening Balance")
            for (c in customers) {
                writer.appendLine("\"${c.name}\",\"${c.phone}\",\"${c.email}\",\"${c.address}\",\"${c.city}\",\"${c.gstin}\",${c.openingBalance}")
            }
        }
        return file
    }

    fun exportProductsCsv(context: Context, products: List<ProductEntity>): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "Products_${DateUtils.formatForFileName()}.csv")
        FileWriter(file).use { writer ->
            writer.appendLine("Name,SKU,Barcode,Unit,Purchase Price,Selling Price,GST Rate,Stock,Min Stock")
            for (p in products) {
                writer.appendLine("\"${p.name}\",\"${p.sku}\",\"${p.barcode}\",\"${p.unit}\",${p.purchasePrice},${p.sellingPrice},${p.gstRate},${p.stockQuantity},${p.minimumStock}")
            }
        }
        return file
    }

    fun exportInvoicesCsv(context: Context, invoices: List<InvoiceEntity>): File {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "Invoices_${DateUtils.formatForFileName()}.csv")
        FileWriter(file).use { writer ->
            writer.appendLine("Invoice No,Date,Customer,Phone,Subtotal,Discount,Tax,Grand Total,Received,Due,Payment Method,Status")
            for (inv in invoices) {
                writer.appendLine("\"${inv.invoiceNumber}\",\"${DateUtils.formatShortDate(inv.invoiceDate)}\",\"${inv.customerNameSnapshot}\",\"${inv.customerPhoneSnapshot}\",${inv.subtotal},${inv.discount},${inv.taxAmount},${inv.grandTotal},${inv.receivedAmount},${inv.dueAmount},\"${inv.paymentMethod}\",\"${inv.status}\"")
            }
        }
        return file
    }
}
