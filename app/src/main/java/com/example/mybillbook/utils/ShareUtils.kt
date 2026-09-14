package com.example.mybillbook.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.data.entity.InvoiceEntity
import java.io.File

object ShareUtils {

    fun sharePdf(context: Context, file: File, title: String = "Share Invoice") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String = "*/*", title: String = "Share File") {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not share file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareInvoiceSummaryText(context: Context, invoice: InvoiceEntity, business: BusinessEntity) {
        val text = buildString {
            appendLine("🧾 *INVOICE: ${invoice.invoiceNumber}*")
            appendLine("From: *${business.businessName}*")
            appendLine("Date: ${DateUtils.formatDate(invoice.invoiceDate)}")
            appendLine("Customer: ${invoice.customerNameSnapshot}")
            appendLine("-------------------------")
            appendLine("Total: ${CurrencyUtils.format(invoice.grandTotal)}")
            appendLine("Paid: ${CurrencyUtils.format(invoice.receivedAmount)}")
            appendLine("Due: ${CurrencyUtils.format(invoice.dueAmount)}")
            if (business.upiId.isNotBlank() && invoice.dueAmount > 0) {
                appendLine("Pay via UPI: ${business.upiId}")
            }
            appendLine("-------------------------")
            appendLine("Thank you for your business!")
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            this.type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Bill Summary"))
    }

    fun sendPaymentReminder(
        context: Context,
        customerPhone: String,
        customerName: String,
        dueAmount: Double,
        upiId: String,
        businessName: String
    ) {
        val displayName = customerName.ifBlank { "Customer" }
        val text = "Dear $displayName, this is a gentle reminder regarding an outstanding balance of ${CurrencyUtils.format(dueAmount)} with $businessName." +
                (if (upiId.isNotBlank()) " You can pay directly via UPI ID: $upiId." else "") +
                " Thank you!"

        if (customerPhone.isNotBlank()) {
            try {
                val uri = Uri.parse("smsto:$customerPhone")
                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra("sms_body", text)
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback to generic text share below
            }
        }

        try {
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Send Payment Reminder"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open sharing apps", Toast.LENGTH_SHORT).show()
        }
    }

    fun makePhoneCall(context: Context, phone: String) {
        if (phone.isBlank()) {
            Toast.makeText(context, "No phone number available", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }
}
