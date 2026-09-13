package com.example.mybillbook.ui.reports

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.repository.ProfitReportData
import com.example.mybillbook.data.repository.SalesReportData
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.FileUtils
import com.example.mybillbook.utils.ShareUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ReportsScreen(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPeriod by remember { mutableStateOf("THIS_MONTH") }
    var salesReport by remember { mutableStateOf<SalesReportData?>(null) }
    var profitSummary by remember { mutableStateOf<ProfitReportData?>(null) }
    var stockValuation by remember { mutableStateOf(0.0) }
    var totalReceivable by remember { mutableStateOf(0.0) }
    var totalPayable by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedPeriod) {
        isLoading = true
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val (startDate, endDate) = when (selectedPeriod) {
            "TODAY" -> Pair(cal.timeInMillis, System.currentTimeMillis())
            "THIS_WEEK" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                Pair(cal.timeInMillis, System.currentTimeMillis())
            }
            else -> Pair(0L, System.currentTimeMillis())
        }

        salesReport = container.reportRepository.getSalesReport(startDate, endDate)
        profitSummary = container.reportRepository.getProfitReport(startDate, endDate)

        val stockItems = container.reportRepository.getStockReport()
        stockValuation = stockItems.sumOf { it.stockValue }

        val dues = container.reportRepository.getCustomerDueReport()
        totalReceivable = dues.sumOf { it.totalDue }

        val purchaseReport = container.reportRepository.getPurchaseReport(0L, System.currentTimeMillis())
        totalPayable = purchaseReport.dueAmount

        isLoading = false
    }

    Scaffold(
        modifier = modifier.testTag("reports_screen"),
        topBar = {
            AppTopBar(
                title = "Business Reports & Analytics",
                subtitle = "Comprehensive financial insights",
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val invoices = container.invoiceRepository.getAllInvoicesDirect()
                                val file = FileUtils.exportInvoicesCsv(context, invoices)
                                ShareUtils.shareFile(context, file, "text/csv", "Sales Invoices CSV")
                            }
                        },
                        modifier = Modifier.testTag("export_csv_action")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = PrimaryBlue)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Period Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "TODAY" to "Today",
                    "THIS_WEEK" to "This Week",
                    "THIS_MONTH" to "This Month",
                    "ALL_TIME" to "All Time"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = selectedPeriod == key,
                        onClick = { selectedPeriod = key },
                        label = { Text(label) },
                        modifier = Modifier.testTag("period_chip_$key")
                    )
                }
            }

            if (isLoading) {
                LoadingState()
            } else {
                val s = salesReport
                val p = profitSummary

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Executive KPI Cards (2x2)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SummaryCard(
                                    title = "Total Sales",
                                    amount = s?.netSales ?: 0.0,
                                    icon = Icons.Default.TrendingUp,
                                    iconTint = SuccessGreen,
                                    iconBg = SuccessGreenLight,
                                    subtitle = "${s?.invoiceCount ?: 0} invoices",
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryCard(
                                    title = "Net Profit",
                                    amount = p?.grossProfit ?: 0.0,
                                    icon = Icons.Default.MonetizationOn,
                                    iconTint = PrimaryBlue,
                                    iconBg = PrimaryBlueLight,
                                    valueColor = if ((p?.grossProfit ?: 0.0) >= 0) SuccessGreen else DangerRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SummaryCard(
                                    title = "Cash Collected",
                                    amount = s?.receivedAmount ?: 0.0,
                                    icon = Icons.Default.Payments,
                                    iconTint = PrimaryBlue,
                                    iconBg = PrimaryBlueLight,
                                    modifier = Modifier.weight(1f)
                                )
                                SummaryCard(
                                    title = "Receivable Due",
                                    amount = s?.dueAmount ?: 0.0,
                                    icon = Icons.Default.AccountBalanceWallet,
                                    iconTint = DangerRed,
                                    iconBg = DangerRedLight,
                                    valueColor = DangerRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Profit & Loss Breakdown Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Profit & Loss Overview",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Gross Sales Revenue", style = MaterialTheme.typography.bodyMedium)
                                    Text(CurrencyUtils.format(p?.salesRevenue ?: 0.0), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cost of Goods Sold (COGS)", style = MaterialTheme.typography.bodyMedium)
                                    Text("- ${CurrencyUtils.format(p?.costOfGoodsSold ?: 0.0)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DangerRed)
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Operating Expenses", style = MaterialTheme.typography.bodyMedium)
                                    Text("- ${CurrencyUtils.format(p?.expenses ?: 0.0)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = DangerRed)
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Estimated Operating Profit", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        CurrencyUtils.format(p?.grossProfit ?: 0.0),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if ((p?.grossProfit ?: 0.0) >= 0) SuccessGreen else DangerRed
                                    )
                                }
                            }
                        }
                    }

                    // Balance Position Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Asset & Liability Position",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Current Stock Asset Value", style = MaterialTheme.typography.bodyMedium)
                                    Text(CurrencyUtils.format(stockValuation), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Customer Outstandings (To Receive)", style = MaterialTheme.typography.bodyMedium)
                                    Text(CurrencyUtils.format(totalReceivable), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = SuccessGreen)
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Supplier Payables (To Pay)", style = MaterialTheme.typography.bodyMedium)
                                    Text(CurrencyUtils.format(totalPayable), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = WarningAmber)
                                }
                            }
                        }
                    }

                    // Export Action Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Data Export & Sharing",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Export full transaction records to CSV spreadsheets for accounting or tax audit.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                PrimaryButton(
                                    text = "Export Sales CSV",
                                    onClick = {
                                        scope.launch {
                                            val invoices = container.invoiceRepository.getAllInvoicesDirect()
                                            val file = FileUtils.exportInvoicesCsv(context, invoices)
                                            ShareUtils.shareFile(context, file, "text/csv", "Sales Invoices CSV")
                                        }
                                    },
                                    icon = Icons.Default.Download,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
