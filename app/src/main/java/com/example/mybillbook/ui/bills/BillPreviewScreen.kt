package com.example.mybillbook.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
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
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.data.entity.InvoiceSettingsEntity
import com.example.mybillbook.domain.model.InvoiceWithItems
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.LoadingState
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.DateUtils
import com.example.mybillbook.utils.PdfGenerator
import com.example.mybillbook.utils.ShareUtils
import java.io.File

@Composable
fun BillPreviewScreen(
    invoiceId: Long,
    container: AppContainer,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var invoiceWithItems by remember { mutableStateOf<InvoiceWithItems?>(null) }
    var business by remember { mutableStateOf<BusinessEntity?>(null) }
    var invoiceSettings by remember { mutableStateOf<InvoiceSettingsEntity?>(null) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(invoiceId) {
        val data = container.invoiceRepository.getInvoiceWithItems(invoiceId)
        val b = container.businessRepository.getBusinessDirect()
        val s = container.businessRepository.getInvoiceSettingsDirect()
        invoiceWithItems = data
        business = b
        invoiceSettings = s

        if (data != null) {
            generatedPdfFile = PdfGenerator.generateInvoicePdf(context, data.invoice, data.items, b, s)
        }
    }

    Scaffold(
        modifier = modifier.testTag("bill_preview_screen"),
        topBar = {
            AppTopBar(
                title = "Invoice Preview",
                subtitle = invoiceWithItems?.invoice?.invoiceNumber,
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = {
                            val f = generatedPdfFile
                            if (f != null) {
                                ShareUtils.sharePdf(context, f, "Invoice ${invoiceWithItems?.invoice?.invoiceNumber}")
                            }
                        },
                        modifier = Modifier.testTag("preview_share_pdf_icon")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = PrimaryBlue)
                    }
                }
            )
        },
        bottomBar = {
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
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Done")
                    }
                    PrimaryButton(
                        text = "Share PDF",
                        onClick = {
                            val f = generatedPdfFile
                            if (f != null) {
                                ShareUtils.sharePdf(context, f, "Invoice ${invoiceWithItems?.invoice?.invoiceNumber}")
                            }
                        },
                        icon = Icons.Default.Share,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }
    ) { paddingValues ->
        val data = invoiceWithItems
        val b = business
        val s = invoiceSettings

        if (data == null || b == null || s == null) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            val inv = data.invoice
            val items = data.items

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    // Document Preview Frame (clean paper style)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineLight, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Header: Business and Invoice No
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = b.businessName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryBlue
                                    )
                                    if (b.address.isNotBlank()) {
                                        Text(text = b.address, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (b.phone.isNotBlank()) {
                                        Text(text = "Phone: ${b.phone}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    if (b.gstin.isNotBlank()) {
                                        Text(text = "GSTIN: ${b.gstin}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "TAX INVOICE",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = PrimaryBlue
                                    )
                                    Text(
                                        text = inv.invoiceNumber,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Date: ${DateUtils.formatDate(inv.invoiceDate)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = OutlineLight)

                            // Bill to
                            Text(
                                text = "BILL TO:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                            Text(
                                text = inv.customerNameSnapshot,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (inv.customerPhoneSnapshot.isNotBlank()) {
                                Text(
                                    text = "Phone: ${inv.customerPhoneSnapshot}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Table Header
                            Surface(
                                color = SurfaceVariantLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Item", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(2f))
                                    Text("Qty", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                    Text("Rate", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                    Text("Total", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                }
                            }

                            // Items rows
                            items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(item.productNameSnapshot, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(2f))
                                    Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text(CurrencyUtils.format(item.rate), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text(CurrencyUtils.format(item.amount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider(color = SurfaceVariantLight)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Totals
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Text("Subtotal:", style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyUtils.format(inv.subtotal), style = MaterialTheme.typography.bodySmall)
                                }
                                if (inv.discount > 0) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                        Text("Discount:", style = MaterialTheme.typography.bodySmall)
                                        Text("-${CurrencyUtils.format(inv.discount)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (inv.taxAmount > 0) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                        Text("Tax:", style = MaterialTheme.typography.bodySmall)
                                        Text(CurrencyUtils.format(inv.taxAmount), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Text("Grand Total:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(CurrencyUtils.format(inv.grandTotal), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Text("Paid:", style = MaterialTheme.typography.bodySmall)
                                    Text(CurrencyUtils.format(inv.receivedAmount), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    Text("Due:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text(CurrencyUtils.format(inv.dueAmount), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = DangerRed)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (b.bankName.isNotBlank() || b.upiId.isNotBlank()) {
                                Text("Payment Details:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                                if (b.bankName.isNotBlank()) {
                                    Text("Bank: ${b.bankName} | A/C: ${b.accountNumber} | IFSC: ${b.ifsc}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                if (b.upiId.isNotBlank()) {
                                    Text("UPI ID: ${b.upiId}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = s.footerText,
                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = TextMuted,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
