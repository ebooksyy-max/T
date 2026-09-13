package com.example.mybillbook.ui.bills

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.home.InvoiceListItem
import com.example.mybillbook.ui.theme.PrimaryBlue
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun BillsScreen(
    viewModel: BillsViewModel,
    onCreateBillClick: () -> Unit,
    onBillClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.testTag("bills_screen"),
        topBar = {
            AppTopBar(
                title = "Invoices & Bills",
                subtitle = "${uiState.filteredInvoices.size} invoices"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateBillClick,
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("create_bill_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Bill")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Bill", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search invoice number, customer...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Status Filter Chips
            val filters = listOf(
                "ALL" to "All",
                "UNPAID" to "Unpaid",
                "PARTIALLY_PAID" to "Partially Paid",
                "PAID" to "Paid",
                "DRAFT" to "Drafts"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (key, label) ->
                    FilterChip(
                        selected = uiState.selectedFilter == key,
                        onClick = { viewModel.onFilterSelected(key) },
                        label = { Text(label) },
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            // Summary row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${CurrencyUtils.format(uiState.totalSales)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Due: ${CurrencyUtils.format(uiState.totalReceivable)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = com.example.mybillbook.ui.theme.DangerRed
                )
            }

            // List of Invoices
            if (uiState.filteredInvoices.isEmpty()) {
                EmptyState(
                    title = "No Invoices Found",
                    description = if (uiState.searchQuery.isNotBlank()) "No invoices match '${uiState.searchQuery}'." else "There are no invoices in this category.",
                    icon = Icons.Default.ReceiptLong,
                    actionText = "Create New Bill",
                    onAction = onCreateBillClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredInvoices) { invoice ->
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
