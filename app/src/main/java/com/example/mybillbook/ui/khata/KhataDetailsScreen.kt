package com.example.mybillbook.ui.khata

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.CustomerEntity
import com.example.mybillbook.data.entity.KhataTransactionEntity
import com.example.mybillbook.data.entity.KhataTransactionType
import com.example.mybillbook.domain.model.CustomerBalanceSummary
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.DateUtils
import com.example.mybillbook.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataDetailsScreen(
    customerId: Long,
    container: AppContainer,
    onBackClick: () -> Unit,
    onCreateBillClick: (Long) -> Unit,
    onRecordPaymentClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customer by remember { mutableStateOf<CustomerEntity?>(null) }
    var balanceSummary by remember { mutableStateOf<CustomerBalanceSummary?>(null) }
    var transactions by remember { mutableStateOf<List<KhataTransactionEntity>>(emptyList()) }
    var businessName by remember { mutableStateOf("Our Store") }
    var upiId by remember { mutableStateOf("") }

    LaunchedEffect(customerId) {
        customer = container.customerRepository.getCustomerByIdDirect(customerId)
        balanceSummary = container.customerRepository.getCustomerBalanceSummary(customerId)
        val b = container.businessRepository.getBusinessDirect()
        businessName = b.businessName
        upiId = b.upiId

        container.customerRepository.getTransactionsForCustomer(customerId).collect { list ->
            transactions = list
        }
    }

    Scaffold(
        modifier = modifier.testTag("khata_details_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer?.name ?: "Customer Ledger", fontWeight = FontWeight.Bold)
                        Text(customer?.phone ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val c = customer
                    val b = balanceSummary
                    if (c != null && b != null && b.netBalance > 0 && c.phone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                ShareUtils.sendPaymentReminder(
                                    context = context,
                                    customerPhone = c.phone,
                                    customerName = c.name,
                                    dueAmount = b.netBalance,
                                    upiId = upiId,
                                    businessName = businessName
                                )
                            },
                            modifier = Modifier.testTag("send_whatsapp_reminder_action")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Send Reminder", tint = WarningAmber)
                        }
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
                    Button(
                        onClick = { onCreateBillClick(customerId) },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.weight(1f).testTag("give_credit_button")
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Give Credit")
                    }

                    Button(
                        onClick = { onRecordPaymentClick(customerId) },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.weight(1f).testTag("accept_payment_button")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accept Payment")
                    }
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Balance Card
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
                                    Text("Net Balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val net = b.netBalance
                                    Text(
                                        text = if (net >= 0) CurrencyUtils.format(net) else "${CurrencyUtils.format(-net)} (Adv)",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (net > 0) DangerRed else SuccessGreen
                                    )
                                    Text(
                                        text = if (net > 0) "Customer will pay you" else if (net < 0) "You will pay customer" else "Settled account",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }

                                if (c.phone.isNotBlank()) {
                                    IconButton(
                                        onClick = { ShareUtils.makePhoneCall(context, c.phone) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlueLight)
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = PrimaryBlue)
                                    }
                                }
                            }
                        }
                    }
                }

                // Table Header for Ledger
                item {
                    Text(
                        text = "Statement Transactions (${transactions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (transactions.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No Ledger Entries",
                            description = "No transactions recorded yet for this customer. Create an invoice or accept payment to begin recording Khata.",
                            icon = Icons.Default.MenuBook
                        )
                    }
                } else {
                    items(transactions) { tx ->
                        KhataTransactionCard(tx = tx)
                    }
                }
            }
        }
    }
}

@Composable
fun KhataTransactionCard(tx: KhataTransactionEntity) {
    val isPayment = tx.transactionType == KhataTransactionType.PAYMENT.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("khata_tx_${tx.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.note.ifBlank { if (isPayment) "Payment Received" else "Credit / Bill Due" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtils.formatDateTime(tx.transactionDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (tx.referenceType.isNotBlank()) {
                    Text(
                        text = "Ref: ${tx.referenceType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (!isPayment) {
                    Text(
                        text = "+ ${CurrencyUtils.format(tx.amount)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = DangerRed
                    )
                    Text(
                        text = "Gave Credit",
                        style = MaterialTheme.typography.labelSmall,
                        color = DangerRed
                    )
                } else {
                    Text(
                        text = "- ${CurrencyUtils.format(tx.amount)}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = SuccessGreen
                    )
                    Text(
                        text = "Received",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}
