package com.example.mybillbook.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.data.entity.InvoiceItemEntity
import com.example.mybillbook.data.entity.InvoiceSettingsEntity
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 36f

    fun generateInvoicePdf(
        context: Context,
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        business: BusinessEntity,
        settings: InvoiceSettingsEntity
    ): File {
        val pdfDocument = PdfDocument()
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = MARGIN + 20f

        // Draw Business Header
        textPaint.color = Color.parseColor("#0F3D78")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 18f
        canvas.drawText(business.businessName.ifBlank { "My Business" }, MARGIN, y, textPaint)

        // Invoice Title on the right
        textPaint.color = Color.parseColor("#0F172A")
        textPaint.textSize = 18f
        val invoiceTitle = "TAX INVOICE"
        val titleWidth = textPaint.measureText(invoiceTitle)
        canvas.drawText(invoiceTitle, PAGE_WIDTH - MARGIN - titleWidth, y, textPaint)

        y += 16f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9f
        textPaint.color = Color.parseColor("#475569")

        if (business.address.isNotBlank()) {
            canvas.drawText(business.address, MARGIN, y, textPaint)
        }
        val invNoText = "Invoice No: ${invoice.invoiceNumber}"
        canvas.drawText(invNoText, PAGE_WIDTH - MARGIN - textPaint.measureText(invNoText), y, textPaint)

        y += 13f
        val cityState = listOf(business.city, business.state, business.pincode).filter { it.isNotBlank() }.joinToString(", ")
        if (cityState.isNotBlank()) {
            canvas.drawText(cityState, MARGIN, y, textPaint)
        }
        val dateText = "Date: ${DateUtils.formatDate(invoice.invoiceDate)}"
        canvas.drawText(dateText, PAGE_WIDTH - MARGIN - textPaint.measureText(dateText), y, textPaint)

        y += 13f
        val phoneGst = listOf(
            if (business.phone.isNotBlank()) "Phone: ${business.phone}" else "",
            if (business.gstin.isNotBlank()) "GSTIN: ${business.gstin}" else ""
        ).filter { it.isNotBlank() }.joinToString(" | ")
        canvas.drawText(phoneGst, MARGIN, y, textPaint)

        val statusText = "Status: ${invoice.status}"
        val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = when (invoice.status) {
                "PAID" -> Color.parseColor("#16A34A")
                "PARTIALLY_PAID" -> Color.parseColor("#D97706")
                "CANCELLED" -> Color.parseColor("#DC2626")
                else -> Color.parseColor("#DC2626")
            }
        }
        canvas.drawText(statusText, PAGE_WIDTH - MARGIN - statusPaint.measureText(statusText), y, statusPaint)

        y += 16f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)

        // Bill To Section
        y += 18f
        textPaint.color = Color.parseColor("#0F3D78")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        canvas.drawText("BILL TO:", MARGIN, y, textPaint)

        y += 14f
        textPaint.color = Color.parseColor("#0F172A")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        canvas.drawText(invoice.customerNameSnapshot.ifBlank { "Walk-in Customer" }, MARGIN, y, textPaint)

        if (invoice.customerPhoneSnapshot.isNotBlank()) {
            y += 12f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.textSize = 9f
            textPaint.color = Color.parseColor("#475569")
            canvas.drawText("Phone: ${invoice.customerPhoneSnapshot}", MARGIN, y, textPaint)
        }

        y += 18f
        // Table Header Box
        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 22f, tableHeaderPaint)

        textPaint.color = Color.parseColor("#0F172A")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 9f

        val colItem = MARGIN + 6f
        val colQty = 290f
        val colRate = 360f
        val colTax = 430f
        val colTotal = 500f

        canvas.drawText("Item Description", colItem, y + 15f, textPaint)
        canvas.drawText("Qty", colQty, y + 15f, textPaint)
        canvas.drawText("Rate", colRate, y + 15f, textPaint)
        canvas.drawText("Tax", colTax, y + 15f, textPaint)
        canvas.drawText("Total", colTotal, y + 15f, textPaint)

        y += 24f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.textSize = 9f

        // Table Rows
        for (item in items) {
            // Check if page overflow
            if (y > PAGE_HEIGHT - 180f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = MARGIN + 20f

                // Re-draw small header on subsequent pages
                textPaint.color = Color.parseColor("#0F3D78")
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${business.businessName} - Invoice ${invoice.invoiceNumber} (Page $pageNumber)", MARGIN, y, textPaint)
                y += 20f
            }

            textPaint.color = Color.parseColor("#0F172A")
            val itemTitle = if (item.productNameSnapshot.length > 38) item.productNameSnapshot.substring(0, 35) + "..." else item.productNameSnapshot
            canvas.drawText(itemTitle, colItem, y + 14f, textPaint)
            canvas.drawText("${item.quantity} ${item.unit}", colQty, y + 14f, textPaint)
            canvas.drawText(CurrencyUtils.format(item.rate), colRate, y + 14f, textPaint)
            canvas.drawText("${item.taxRate}%", colTax, y + 14f, textPaint)
            canvas.drawText(CurrencyUtils.format(item.amount), colTotal, y + 14f, textPaint)

            y += 20f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        }

        // Totals Section
        y += 15f
        val totalsX = 350f
        val valuesX = PAGE_WIDTH - MARGIN

        fun drawTotalLine(label: String, value: String, isBold: Boolean = false) {
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, if (isBold) Typeface.BOLD else Typeface.NORMAL)
            textPaint.color = if (isBold) Color.parseColor("#0F3D78") else Color.parseColor("#334155")
            textPaint.textSize = if (isBold) 11f else 9f
            canvas.drawText(label, totalsX, y, textPaint)
            val valWidth = textPaint.measureText(value)
            canvas.drawText(value, valuesX - valWidth, y, textPaint)
            y += 16f
        }

        drawTotalLine("Subtotal:", CurrencyUtils.format(invoice.subtotal))
        if (invoice.discount > 0) {
            drawTotalLine("Discount:", "-${CurrencyUtils.format(invoice.discount)}")
        }
        if (invoice.taxAmount > 0) {
            drawTotalLine("Tax:", CurrencyUtils.format(invoice.taxAmount))
        }
        canvas.drawLine(totalsX, y - 4f, PAGE_WIDTH - MARGIN, y - 4f, linePaint)
        drawTotalLine("Grand Total:", CurrencyUtils.format(invoice.grandTotal), isBold = true)
        drawTotalLine("Received Amount:", CurrencyUtils.format(invoice.receivedAmount))
        drawTotalLine("Balance Due:", CurrencyUtils.format(invoice.dueAmount), isBold = invoice.dueAmount > 0)

        // Payment Info & Bank Details on the bottom left
        var footerY = y + 10f
        textPaint.textSize = 8.5f
        textPaint.color = Color.parseColor("#475569")
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        if (business.bankName.isNotBlank() || business.upiId.isNotBlank()) {
            canvas.drawText("BANK & PAYMENT DETAILS:", MARGIN, footerY, textPaint)
            footerY += 12f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (business.bankName.isNotBlank()) {
                canvas.drawText("Bank: ${business.bankName} | A/C: ${business.accountNumber} | IFSC: ${business.ifsc}", MARGIN, footerY, textPaint)
                footerY += 12f
            }
            if (business.upiId.isNotBlank()) {
                canvas.drawText("UPI ID: ${business.upiId}", MARGIN, footerY, textPaint)
                footerY += 12f
            }
        }

        if (settings.termsAndConditions.isNotBlank()) {
            footerY += 6f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("TERMS & CONDITIONS:", MARGIN, footerY, textPaint)
            footerY += 11f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            settings.termsAndConditions.split("\n").take(2).forEach { line ->
                canvas.drawText(line, MARGIN, footerY, textPaint)
                footerY += 11f
            }
        }

        // Footer Thank you note centered
        val footerNote = settings.footerText.ifBlank { "Thank you for your business!" }
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        textPaint.color = Color.parseColor("#64748B")
        textPaint.textSize = 9f
        val noteWidth = textPaint.measureText(footerNote)
        canvas.drawText(footerNote, (PAGE_WIDTH - noteWidth) / 2f, PAGE_HEIGHT - MARGIN, textPaint)

        pdfDocument.finishPage(page)

        // Write to cache or documents file
        val invoicesDir = File(context.cacheDir, "invoices").apply { mkdirs() }
        val pdfFile = File(invoicesDir, "${invoice.invoiceNumber.replace("/", "_")}.pdf")
        FileOutputStream(pdfFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return pdfFile
    }
}
