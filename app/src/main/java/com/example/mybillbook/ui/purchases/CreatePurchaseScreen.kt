package com.example.mybillbook.ui.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.*
import com.example.mybillbook.domain.model.BillItemInput
import com.example.mybillbook.domain.usecase.BillingCalculator
import com.example.mybillbook.ui.bills.ProductPickerDialog
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun CreatePurchaseScreen(
    container: AppContainer,
    onBackClick: () -> Unit,
    onPurchaseSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var purchaseNumber by remember { mutableStateOf("PUR-${(1000..9999).random()}") }
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var allProducts by remember { mutableStateOf<List<ProductEntity>>(emptyList()) }
    var items by remember { mutableStateOf<List<BillItemInput>>(emptyList()) }
    var paidAmountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethods.BANK) }
    var notes by remember { mutableStateOf("") }
    var showProductPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        container.productRepository.allProducts.collect {
            allProducts = it
        }
    }

    val calc = remember(items, paidAmountText) {
        val paid = paidAmountText.toDoubleOrNull() ?: 0.0
        BillingCalculator.calculateBill(items, additionalDiscount = 0.0, receivedAmount = paid)
    }

    Scaffold(
        modifier = modifier.testTag("create_purchase_screen"),
        topBar = {
            AppTopBar(
                title = "Record Purchase (Stock In)",
                subtitle = purchaseNumber,
                onBackClick = onBackClick
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Purchase", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(
                            CurrencyUtils.format(calc.grandTotal),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                    }

                    PrimaryButton(
                        text = "Save Purchase",
                        onClick = {
                            if (items.isEmpty()) {
                                errorMessage = "Please add at least one product purchased"
                                return@PrimaryButton
                            }
                            isSaving = true
                            scope.launch {
                                val purchase = PurchaseEntity(
                                    purchaseNumber = purchaseNumber,
                                    supplierId = null,
                                    supplierNameSnapshot = supplierName.ifBlank { "Wholesale Vendor" },
                                    purchaseDate = System.currentTimeMillis(),
                                    subtotal = calc.subtotal,
                                    taxAmount = calc.taxAmount,
                                    grandTotal = calc.grandTotal,
                                    paidAmount = calc.receivedAmount,
                                    dueAmount = calc.dueAmount,
                                    paymentMethod = paymentMethod,
                                    status = if (calc.dueAmount <= 0) "PAID" else "UNPAID",
                                    notes = notes.trim()
                                )

                                val purchaseItems = calc.items.map { item ->
                                    PurchaseItemEntity(
                                        purchaseId = 0L,
                                        productId = item.productId ?: 0L,
                                        productNameSnapshot = item.productName,
                                        quantity = item.quantity,
                                        unit = item.unit,
                                        rate = item.rate,
                                        taxRate = item.taxRate,
                                        taxAmount = item.taxAmount,
                                        amount = item.totalAmount
                                    )
                                }

                                container.purchaseRepository.savePurchase(purchase, purchaseItems)
                                isSaving = false
                                onPurchaseSaved()
                            }
                        },
                        isLoading = isSaving,
                        enabled = items.isNotEmpty()
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (errorMessage != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = DangerRedLight)) {
                        Text(
                            text = errorMessage ?: "",
                            color = DangerRed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Supplier Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Supplier / Vendor Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = supplierName,
                                onValueChange = { supplierName = it },
                                label = { Text("Supplier Name") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f).testTag("supplier_name_input")
                            )
                            OutlinedTextField(
                                value = purchaseNumber,
                                onValueChange = { purchaseNumber = it },
                                label = { Text("Bill / Inv #") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Add Items Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Purchased Items (${items.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = { showProductPicker = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("add_purchase_item_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Product")
                    }
                }
            }

            if (items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showProductPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No items added yet. Tap to select products to restock.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                itemsIndexed(items) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.productName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                IconButton(
                                    onClick = {
                                        val m = items.toMutableList()
                                        m.removeAt(index)
                                        items = m
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = DangerRed)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = item.quantity.toString(),
                                    onValueChange = {
                                        val q = it.toDoubleOrNull() ?: 1.0
                                        val m = items.toMutableList()
                                        m[index] = m[index].copy(quantity = q)
                                        items = m
                                    },
                                    label = { Text("Qty (${item.unit})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = item.rate.toString(),
                                    onValueChange = {
                                        val r = it.toDoubleOrNull() ?: 0.0
                                        val m = items.toMutableList()
                                        m[index] = m[index].copy(rate = r)
                                        items = m
                                    },
                                    label = { Text("Cost Rate (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Payment to Vendor
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Payment to Vendor", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = paidAmountText,
                                onValueChange = { paidAmountText = it },
                                label = { Text("Amount Paid Now (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Column(
                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("Payable Balance", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text(
                                    CurrencyUtils.format(calc.dueAmount),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (calc.dueAmount > 0) WarningAmber else SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showProductPicker) {
        ProductPickerDialog(
            products = allProducts,
            onProductSelected = { p ->
                val newItem = BillItemInput(
                    productId = p.id,
                    productName = p.name,
                    quantity = 1.0,
                    unit = p.unit,
                    rate = if (p.purchasePrice > 0) p.purchasePrice else p.sellingPrice * 0.7,
                    discount = 0.0,
                    taxRate = p.gstRate,
                    currentStock = p.stockQuantity
                )
                items = items + newItem
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }
}
