package com.example.mybillbook.ui.customers

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.CustomerEntity
import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.data.entity.KhataTransactionEntity
import com.example.mybillbook.domain.model.CustomerBalanceSummary
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.home.InvoiceListItem
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.DateUtils
import com.example.mybillbook.utils.ShareUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    customerId: Long,
    container: AppContainer,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onCreateBillClick: (Long) -> Unit,
    onRecordPaymentClick: (Long) -> Unit,
    onViewBillClick: (Long) -> Unit,
    onViewKhataClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    var balanceSummary by remember { mutableStateOf<CustomerBalanceSummary?>(null) }
    var invoices by remember { mutableStateOf<List<InvoiceEntity>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        customer = container.customerRepository.getCustomerByIdDirect(customerId)
        balanceSummary = container.customerRepository.getCustomerBalanceSummary(customerId)
        container.invoiceRepository.allInvoices.collect { all ->
            invoices = all.filter { it.customerId == customerId }
        }
    }

    Scaffold(
        modifier = modifier.testTag("customer_details_screen"),
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEditClick(customerId) },
                        modifier = Modifier.testTag("edit_customer_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer")
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.testTag("delete_customer_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = DangerRed)
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
                        onClick = { onCreateBillClick(customerId) },
                        modifier = Modifier.weight(1f).testTag("customer_new_bill_button")
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Bill")
                    }

                    PrimaryButton(
                        text = "Record Payment",
                        onClick = { onRecordPaymentClick(customerId) },
                        icon = Icons.Default.Payments,
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }
    ) { paddingValues ->
        val c = customer
        val b = balanceSummary
        if (c == null || b == null) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance Banner Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Current Net Balance",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val net = b.netBalance
                            Text(
                                text = if (net >= 0) CurrencyUtils.format(net) else "${CurrencyUtils.format(-net)} (Advance)",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (net > 0) DangerRed else SuccessGreen
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Total Sales", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(CurrencyUtils.format(b.totalCredit), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Received", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text(CurrencyUtils.format(b.totalPayment), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = SuccessGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onViewKhataClick(c.id) },
                                modifier = Modifier.fillMaxWidth().testTag("view_full_ledger_button")
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("View Full Statement / Khata Ledger")
                            }
                        }
                    }
                }

                // Contact & Address Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Contact & Tax Information", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                            Spacer(modifier = Modifier.height(10.dp))

                            if (c.phone.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Phone", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        Text(c.phone, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row {
                                        IconButton(onClick = { ShareUtils.makePhoneCall(context, c.phone) }) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = PrimaryBlue)
                                        }
                                        if (b.netBalance > 0) {
                                            IconButton(onClick = {
                                                ShareUtils.sendPaymentReminder(
                                                    context = context,
                                                    customerPhone = c.phone,
                                                    customerName = c.name,
                                                    dueAmount = b.netBalance,
                                                    upiId = "",
                                                    businessName = "Our Store"
                                                )
                                            }) {
                                                Icon(Icons.Default.NotificationsActive, contentDescription = "Reminder", tint = WarningAmber)
                                            }
                                        }
                                    }
                                }
                            }

                            if (c.email.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Email", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(c.email, style = MaterialTheme.typography.bodyMedium)
                            }

                            if (c.address.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Billing Address", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text("${c.address}, ${c.city} ${c.state} ${c.pincode}".trim(), style = MaterialTheme.typography.bodyMedium)
                            }

                            if (c.gstin.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("GSTIN", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(c.gstin, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }

                // Invoices History
                item {
                    Text(
                        text = "Invoices History (${invoices.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (invoices.isEmpty()) {
                    item {
                        Text(
                            text = "No invoices generated for this customer yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                } else {
                    items(invoices) { inv ->
                        InvoiceListItem(
                            invoice = inv,
                            onClick = { onViewBillClick(inv.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDialog(
            title = "Delete Customer?",
            message = "Are you sure you want to delete ${customer?.name}? This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    container.customerRepository.deleteCustomer(customerId)
                    onBackClick()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
