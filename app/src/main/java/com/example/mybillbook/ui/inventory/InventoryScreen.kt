package com.example.mybillbook.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mybillbook.data.entity.ProductEntity
import com.example.mybillbook.ui.components.*
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun InventoryScreen(
    viewModel: ProductViewModel,
    onAddProductClick: () -> Unit,
    onProductClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var adjustingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        modifier = modifier.testTag("inventory_screen"),
        topBar = {
            AppTopBar(
                title = "Inventory & Products",
                subtitle = "${uiState.products.size} items"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProductClick,
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Item", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stock Summary Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Stock Value",
                    amount = uiState.totalStockValue,
                    icon = Icons.Default.Inventory2,
                    modifier = Modifier.weight(1.2f)
                )
                SummaryCard(
                    title = "Low Stock Alerts",
                    amount = uiState.lowStockCount.toDouble(),
                    icon = Icons.Default.Inventory2,
                    iconTint = DangerRed,
                    iconBg = DangerRedLight,
                    valueColor = if (uiState.lowStockCount > 0) DangerRed else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            AppSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = "Search item name, SKU or barcode...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Category & Low Stock Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == "ALL" && uiState.selectedCategory == null,
                    onClick = {
                        viewModel.onFilterSelected("ALL")
                        viewModel.onCategorySelected(null)
                    },
                    label = { Text("All Items") },
                    modifier = Modifier.testTag("filter_all_items")
                )

                FilterChip(
                    selected = uiState.selectedFilter == "LOW_STOCK",
                    onClick = { viewModel.onFilterSelected("LOW_STOCK") },
                    label = { Text("Low Stock (${uiState.lowStockCount})") },
                    modifier = Modifier.testTag("filter_low_stock")
                )

                uiState.categories.forEach { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat.id,
                        onClick = { viewModel.onCategorySelected(cat.id) },
                        label = { Text(cat.name) },
                        modifier = Modifier.testTag("category_chip_${cat.name}")
                    )
                }
            }

            // Product List
            if (uiState.filteredProducts.isEmpty()) {
                EmptyState(
                    title = "No Products Found",
                    description = if (uiState.searchQuery.isNotBlank()) "No items match '${uiState.searchQuery}'." else "Add your products to manage pricing, inventory and fast billing.",
                    icon = Icons.Default.Inventory2,
                    actionText = "Add First Product",
                    onAction = onAddProductClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.filteredProducts) { product ->
                        ProductItemCard(
                            product = product,
                            onProductClick = { onProductClick(product.id) },
                            onAdjustStockClick = { adjustingProduct = product }
                        )
                    }
                }
            }
        }
    }

    if (adjustingProduct != null) {
        StockAdjustmentDialog(
            product = adjustingProduct!!,
            onDismiss = { adjustingProduct = null },
            onConfirm = { type, qty, note ->
                viewModel.adjustStock(adjustingProduct!!.id, type, qty, note)
                adjustingProduct = null
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    onProductClick: () -> Unit,
    onAdjustStockClick: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minimumStock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.name}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (product.sku.isNotBlank()) {
                            Text(
                                text = "SKU: ${product.sku}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        if (product.gstRate > 0) {
                            Text(
                                text = "| GST ${product.gstRate}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                StatusChip(status = if (isLowStock) "LOW_STOCK" else "IN_STOCK")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sale: ${CurrencyUtils.format(product.sellingPrice)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    if (product.purchasePrice > 0) {
                        Text(
                            text = "Purchase: ${CurrencyUtils.format(product.purchasePrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${product.stockQuantity} ${product.unit}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isLowStock) DangerRed else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Min: ${product.minimumStock} ${product.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onAdjustStockClick,
                        modifier = Modifier.testTag("adjust_stock_btn_${product.name}")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Adjust Stock", tint = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String) -> Unit
) {
    var adjType by remember { mutableStateOf("ADD") }
    var qtyText by remember { mutableStateOf("1") }
    var reasonText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stock_adjustment_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Adjust Stock: ${product.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Text(
                    text = "Current Stock: ${product.stockQuantity} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = adjType == "ADD",
                        onClick = { adjType = "ADD" },
                        label = { Text("+ Add") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = adjType == "REDUCE",
                        onClick = { adjType = "REDUCE" },
                        label = { Text("- Reduce") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = adjType == "SET",
                        onClick = { adjType = "SET" },
                        label = { Text("Set Exactly") },
                        modifier = Modifier.weight(1.2f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text(if (adjType == "SET") "New Stock Count" else "Quantity to adjust") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("adjust_qty_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Reason (e.g., Stock purchase, Damaged, Count correction)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    PrimaryButton(
                        text = "Confirm",
                        onClick = {
                            val q = qtyText.toDoubleOrNull() ?: 0.0
                            if (q >= 0) {
                                onConfirm(adjType, q, reasonText)
                            }
                        }
                    )
                }
            }
        }
    }
}
