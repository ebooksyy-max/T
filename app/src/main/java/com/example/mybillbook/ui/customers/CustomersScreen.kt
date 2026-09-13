package com.example.mybillbook.ui.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun CustomersScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (Long) -> Unit,
    onAddCustomerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.testTag("customers_screen"),
        topBar = {
            AppTopBar(
                title = "Customers (Parties)",
                subtitle = "${uiState.customers.size} registered customers"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Customer")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Customer", fontWeight = FontWeight.Bold)
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
                placeholder = "Search customer by name or phone...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Balance Summary Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Receivable: ${CurrencyUtils.format(uiState.totalDue)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = DangerRed
                )
                if (uiState.totalAdvance > 0) {
                    Text(
                        text = "Advance: ${CurrencyUtils.format(uiState.totalAdvance)}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = SuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Customers List
            if (uiState.filteredCustomers.isEmpty()) {
                EmptyState(
                    title = "No Customers Found",
                    description = if (uiState.searchQuery.isNotBlank()) "No customer matches '${uiState.searchQuery}'." else "Add your customers to track sales, invoices, and ledger balance.",
                    icon = Icons.Default.Person,
                    actionText = "Add First Customer",
                    onAction = onAddCustomerClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredCustomers) { item ->
                        CustomerListItem(
                            customerWithBalance = item,
                            onClick = { onCustomerClick(item.customer.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerListItem(
    customerWithBalance: CustomerWithBalance,
    onClick: () -> Unit
) {
    val customer = customerWithBalance.customer
    val balance = customerWithBalance.balance

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("customer_item_${customer.name}"),
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
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Letter Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customer.name.firstOrNull()?.uppercase() ?: "C",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (customer.phone.isNotBlank()) customer.phone else "No phone recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Balance Display
            Column(horizontalAlignment = Alignment.End) {
                val net = balance.netBalance
                when {
                    net > 0 -> {
                        Text(
                            text = CurrencyUtils.format(net),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DangerRed
                        )
                        Text(
                            text = "Due (To Collect)",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed
                        )
                    }
                    net < 0 -> {
                        Text(
                            text = CurrencyUtils.format(-net),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = SuccessGreen
                        )
                        Text(
                            text = "Advance",
                            style = MaterialTheme.typography.labelSmall,
                            color = SuccessGreen
                        )
                    }
                    else -> {
                        Text(
                            text = "Settled",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextMuted
                        )
                        Text(
                            text = "₹0.00",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
