package com.example.mybillbook.ui.bills

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.*
import com.example.mybillbook.domain.model.InvoiceWithItems
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.DateUtils
import com.example.mybillbook.utils.PdfGenerator
import com.example.mybillbook.utils.ShareUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailsScreen(
    invoiceId: Long,
    container: AppContainer,
    onBackClick: () -> Unit,
    onRecordPaymentClick: (Long, Long?) -> Unit,
    onPreviewPdfClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var invoiceWithItems by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var business by remember { mutableStateOf<BusinessEntity?>(null) }
    var invoiceSettings by remember { mutableStateOf<InvoiceSettingsEntity?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        invoiceWithItems = container.invoiceRepository.getInvoiceWithItems(invoiceId)
        business = container.businessRepository.getBusinessDirect()
        invoiceSettings = container.businessRepository.getInvoiceSettingsDirect()
    }

    Scaffold(
        modifier = modifier.testTag("bill_details_screen"),
        topBar = {
            TopAppBar(
                title = { Text(invoiceWithItems?.invoice?.invoiceNumber ?: "Invoice Details", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val inv = invoiceWithItems?.invoice
                            val items = invoiceWithItems?.items
                            val b = business
                            val s = invoiceSettings
                            if (inv != null && items != null && b != null && s != null) {
                                val pdfFile = PdfGenerator.generateInvoicePdf(context, inv, items, b, s)
                                ShareUtils.sharePdf(context, pdfFile, "Invoice ${inv.invoiceNumber}")
                            }
                        },
                        modifier = Modifier.testTag("share_pdf_action")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = PrimaryBlue)
                    }
                }
            )
        },
        bottomBar = {
            val inv = invoiceWithItems?.invoice
            if (inv != null && inv.dueAmount > 0 && inv.status != InvoiceStatus.CANCELLED.name) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "Reminder",
                            icon = Icons.Default.NotificationsNone,
                            onClick = {
                                ShareUtils.sendPaymentReminder(
                                    context = context,
                                    customerPhone = inv.customerPhoneSnapshot,
                                    customerName = inv.customerNameSnapshot,
                                    dueAmount = inv.dueAmount,
                                    upiId = business?.upiId ?: "",
                                    businessName = business?.businessName ?: "Our Store"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_reminder_button")
                        )

                        PrimaryButton(
                            text = "Record Payment",
                            onClick = { onRecordPaymentClick(inv.id, inv.customerId) },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val data = invoiceWithItems
        if (data == null) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            val invoice = data.invoice
            val items = data.items

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = invoice.invoiceNumber,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = "Date: ${DateUtils.formatDateTime(invoice.invoiceDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusChip(status = invoice.status)
                            }
                        }
                    }
                }

                // Customer Snapshot Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Billed To",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = invoice.customerNameSnapshot,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (invoice.customerPhoneSnapshot.isNotBlank()) {
                                    Text(
                                        text = invoice.customerPhoneSnapshot,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (invoice.customerPhoneSnapshot.isNotBlank()) {
                                IconButton(
                                    onClick = { ShareUtils.makePhoneCall(context, invoice.customerPhoneSnapshot) },
                                    modifier = Modifier.testTag("call_customer_button")
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Call Customer", tint = PrimaryBlue)
                                }
                            }
                        }
                    }
                }

                // Items Breakdown Table Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Billed Items (${items.size})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.productNameSnapshot,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                        )
                                        Text(
                                            text = "${item.quantity} ${item.unit} × ${CurrencyUtils.format(item.rate)}" +
                                                    if (item.taxRate > 0) " (+${item.taxRate}% GST)" else "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = CurrencyUtils.format(item.amount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                HorizontalDivider(color = OutlineLight, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }

                // Financial Totals Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyUtils.format(invoice.subtotal), style = MaterialTheme.typography.bodyMedium)
                            }
                            if (invoice.discount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Discount", style = MaterialTheme.typography.bodyMedium)
                                    Text("-${CurrencyUtils.format(invoice.discount)}", style = MaterialTheme.typography.bodyMedium, color = DangerRed)
                                }
                            }
                            if (invoice.taxAmount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tax (GST)", style = MaterialTheme.typography.bodyMedium)
                                    Text(CurrencyUtils.format(invoice.taxAmount), style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Grand Total", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(CurrencyUtils.format(invoice.grandTotal), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Received (${invoice.paymentMethod})", style = MaterialTheme.typography.bodyMedium)
                                Text(CurrencyUtils.format(invoice.receivedAmount), style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Balance Due", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(CurrencyUtils.format(invoice.dueAmount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = if (invoice.dueAmount > 0) DangerRed else TextMuted)
                            }
                        }
                    }
                }

                // Quick Action Buttons (PDF, Text share, Cancel)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onPreviewPdfClick(invoice.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.weight(1f).testTag("preview_pdf_button")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View PDF")
                            }

                            OutlinedButton(
                                onClick = {
                                    val b = business
                                    if (b != null) {
                                        ShareUtils.shareInvoiceSummaryText(context, invoice, b)
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("share_text_button")
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Text")
                            }
                        }

                        if (invoice.status != InvoiceStatus.CANCELLED.name) {
                            TextButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.fillMaxWidth().testTag("cancel_invoice_button"),
                                colors = ButtonDefaults.textButtonColors(contentColor = DangerRed)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel this Invoice (Restores Stock)")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        ConfirmDialog(
            title = "Cancel Invoice?",
            message = "Cancelling this invoice will restore all product inventory quantities and mark this bill as CANCELLED. Are you sure?",
            confirmText = "Cancel Invoice",
            isDestructive = true,
            onConfirm = {
                showCancelDialog = false
                scope.launch {
                    container.invoiceRepository.cancelInvoice(invoiceId)
                    invoiceWithItems = container.invoiceRepository.getInvoiceWithItems(invoiceId)
                }
            },
            onDismiss = { showCancelDialog = false }
        )
    }
}
