package com.example.mybillbook.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.DateUtils

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateBillClick: () -> Unit,
    onBillClick: (Long) -> Unit,
    onViewAllBillsClick: () -> Unit,
    onRecordPaymentClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onAddCustomerClick: () -> Unit,
    onInventoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.testTag("home_screen"),
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.Center) {
                        Text(
                            text = uiState.business.businessName.ifBlank { "My Bill Book" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                        Text(
                            text = if (uiState.business.ownerName.isNotBlank()) "Owner: ${uiState.business.ownerName}" else "Offline Billing & Khata",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Low Stock Warning Banner
                if (uiState.lowStockCount > 0) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInventoryClick() }
                                .testTag("low_stock_banner"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = WarningAmberLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${uiState.lowStockCount} items low on stock",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Tap to review inventory and replenish stock",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 2x2 Financial Summary Metrics
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "Today's Sales",
                                amount = uiState.todaySales,
                                icon = Icons.Default.TrendingUp,
                                iconTint = SuccessGreen,
                                iconBg = SuccessGreenLight,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "Today's Collection",
                                amount = uiState.todayCollection,
                                icon = Icons.Default.Payments,
                                iconTint = PrimaryBlue,
                                iconBg = PrimaryBlueLight,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SummaryCard(
                                title = "To Collect (Due)",
                                amount = uiState.totalReceivable,
                                icon = Icons.Default.CallReceived,
                                iconTint = DangerRed,
                                iconBg = DangerRedLight,
                                valueColor = DangerRed,
                                modifier = Modifier.weight(1f)
                            )
                            SummaryCard(
                                title = "To Pay (Payable)",
                                amount = uiState.totalPayable,
                                icon = Icons.Default.CallMade,
                                iconTint = WarningAmber,
                                iconBg = WarningAmberLight,
                                valueColor = WarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Quick Action Buttons
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.AddShoppingCart,
                            label = "+ New Bill",
                            bg = PrimaryBlue,
                            tint = Color.White,
                            onClick = onCreateBillClick
                        )
                        QuickActionButton(
                            icon = Icons.Default.AccountBalanceWallet,
                            label = "Payment",
                            bg = SecondaryTeal,
                            tint = Color.White,
                            onClick = onRecordPaymentClick
                        )
                        QuickActionButton(
                            icon = Icons.Default.AddBox,
                            label = "Add Item",
                            bg = SurfaceVariantLight,
                            tint = PrimaryBlue,
                            onClick = onAddProductClick
                        )
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Add Party",
                            bg = SurfaceVariantLight,
                            tint = PrimaryBlue,
                            onClick = onAddCustomerClick
                        )
                    }
                }

                // Recent Invoices Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Invoices",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (uiState.recentInvoices.isNotEmpty()) {
                            TextButton(
                                onClick = onViewAllBillsClick,
                                modifier = Modifier.testTag("view_all_bills_button")
                            ) {
                                Text("View All")
                            }
                        }
                    }
                }

                if (uiState.recentInvoices.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No Invoices Yet",
                            description = "Start your business by creating your very first sales invoice or load sample demo data.",
                            icon = Icons.Default.ReceiptLong,
                            actionText = "Create New Bill",
                            onAction = onCreateBillClick
                        )
                    }
                } else {
                    items(uiState.recentInvoices) { invoice ->
                        InvoiceListItem(
                            invoice = invoice,
                            onClick = { onBillClick(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    bg: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("quick_action_${label.replace(" ", "_").lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun InvoiceListItem(
    invoice: InvoiceEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_item_${invoice.invoiceNumber}"),
        shape = RoundedCornerShape(12.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.invoiceNumber,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusChip(status = invoice.status)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = invoice.customerNameSnapshot.ifBlank { "Walk-in Customer" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtils.formatDate(invoice.invoiceDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(invoice.grandTotal),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (invoice.dueAmount > 0) {
                    Text(
                        text = "Due: ${CurrencyUtils.format(invoice.dueAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = DangerRed
                    )
                }
            }
        }
    }
}
