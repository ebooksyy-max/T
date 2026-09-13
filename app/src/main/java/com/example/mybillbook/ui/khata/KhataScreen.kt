package com.example.mybillbook.ui.khata

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.domain.model.CustomerBalanceSummary
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.customers.CustomerListItem
import com.example.mybillbook.ui.customers.CustomerWithBalance
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun KhataScreen(
    container: AppContainer,
    onCustomerKhataClick: (Long) -> Unit,
    onRecordPaymentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var customersWithBalance by remember { mutableStateOf<List<CustomerWithBalance>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, DUE, ADVANCE
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        container.customerRepository.allCustomers.collect { list ->
            customersWithBalance = list.map { c ->
                val bal = container.customerRepository.getCustomerBalanceSummary(c.id)
                CustomerWithBalance(c, bal)
            }
            isLoading = false
        }
    }

    val totalDue = customersWithBalance.filter { it.balance.netBalance > 0 }.sumOf { it.balance.netBalance }
    val totalAdv = customersWithBalance.filter { it.balance.netBalance < 0 }.sumOf { -it.balance.netBalance }

    val filteredList = customersWithBalance.filter { item ->
        val matchesFilter = when (selectedFilter) {
            "DUE" -> item.balance.netBalance > 0
            "ADVANCE" -> item.balance.netBalance < 0
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.customer.name.contains(searchQuery, ignoreCase = true) ||
                item.customer.phone.contains(searchQuery)
        matchesFilter && matchesSearch
    }

    Scaffold(
        modifier = modifier.testTag("khata_screen"),
        topBar = {
            AppTopBar(
                title = "Customer Khata (Ledger)",
                subtitle = "Manage dues, credits & ledger"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRecordPaymentClick,
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("record_payment_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Payments, contentDescription = "Record Payment")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Record Payment", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Metrics cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "You'll Get (Due)",
                    amount = totalDue,
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = DangerRed,
                    iconBg = DangerRedLight,
                    valueColor = DangerRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "You'll Give (Advance)",
                    amount = totalAdv,
                    icon = Icons.Default.Payments,
                    iconTint = SuccessGreen,
                    iconBg = SuccessGreenLight,
                    valueColor = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            AppSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search customer...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${customersWithBalance.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "DUE",
                    onClick = { selectedFilter = "DUE" },
                    label = { Text("You'll Get") }
                )
                FilterChip(
                    selected = selectedFilter == "ADVANCE",
                    onClick = { selectedFilter = "ADVANCE" },
                    label = { Text("You'll Give") }
                )
            }

            if (isLoading) {
                LoadingState()
            } else if (filteredList.isEmpty()) {
                EmptyState(
                    title = "No Khata Accounts",
                    description = "All customer ledger balances are clean or no customers match your query.",
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList) { item ->
                        CustomerListItem(
                            customerWithBalance = item,
                            onClick = { onCustomerKhataClick(item.customer.id) }
                        )
                    }
                }
            }
        }
    }
}
