package com.example.mybillbook.ui.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.PurchaseEntity
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import com.example.mybillbook.utils.DateUtils

@Composable
fun PurchasesScreen(
    container: AppContainer,
    onCreatePurchaseClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var purchases by remember { mutableStateOf<List<PurchaseEntity>>(emptyList()) }
    var totalPurchases by remember { mutableStateOf(0.0) }
    var totalPayable by remember { mutableStateOf(0.0) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        container.purchaseRepository.allPurchases.collect { list ->
            purchases = list
            totalPurchases = list.sumOf { it.grandTotal }
            totalPayable = list.sumOf { it.dueAmount }
            isLoading = false
        }
    }

    val filtered = purchases.filter {
        searchQuery.isBlank() ||
                it.purchaseNumber.contains(searchQuery, ignoreCase = true) ||
                it.supplierNameSnapshot.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier.testTag("purchases_screen"),
        topBar = {
            AppTopBar(
                title = "Purchases (Stock In)",
                subtitle = "${purchases.size} purchase bills",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePurchaseClick,
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("create_purchase_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Purchase")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Purchase", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Purchases",
                    amount = totalPurchases,
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Payable",
                    amount = totalPayable,
                    icon = Icons.Default.ShoppingCart,
                    iconTint = WarningAmber,
                    iconBg = WarningAmberLight,
                    valueColor = WarningAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            AppSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search purchase bill or supplier...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (isLoading) {
                LoadingState()
            } else if (filtered.isEmpty()) {
                EmptyState(
                    title = "No Purchases Recorded",
                    description = "Record your supplier purchases to automatically update stock quantities and cost of goods.",
                    icon = Icons.Default.ShoppingCart,
                    actionText = "Record Purchase",
                    onAction = onCreatePurchaseClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { p ->
                        PurchaseItemCard(purchase = p)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseItemCard(purchase: PurchaseEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("purchase_item_${purchase.purchaseNumber}"),
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
                Text(
                    text = purchase.purchaseNumber,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = purchase.supplierNameSnapshot,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = DateUtils.formatDate(purchase.purchaseDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(purchase.grandTotal),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (purchase.dueAmount > 0) {
                    Text(
                        text = "Payable: ${CurrencyUtils.format(purchase.dueAmount)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = WarningAmber
                    )
                } else {
                    StatusChip(status = "PAID")
                }
            }
        }
    }
}
