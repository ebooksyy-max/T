package com.example.mybillbook.ui.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mybillbook.data.entity.CustomerEntity
import com.example.mybillbook.data.entity.PaymentMethods
import com.example.mybillbook.data.entity.ProductEntity
import com.example.mybillbook.domain.model.BillItemInput
import com.example.mybillbook.ui.components.AppSearchBar
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils

@Composable
fun CreateBillScreen(
    viewModel: CreateBillViewModel,
    onBackClick: () -> Unit,
    onInvoiceSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("create_bill_screen"),
        topBar = {
            AppTopBar(
                title = "New Sales Bill",
                subtitle = uiState.invoiceNumber,
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grand Total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyUtils.format(uiState.calculation.grandTotal),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.saveInvoice(isDraft = true, onSuccess = onInvoiceSaved) },
                                enabled = !uiState.isSaving && uiState.items.isNotEmpty(),
                                modifier = Modifier.testTag("save_draft_button")
                            ) {
                                Text("Draft")
                            }

                            PrimaryButton(
                                text = "Save Bill",
                                onClick = { viewModel.saveInvoice(isDraft = false, onSuccess = onInvoiceSaved) },
                                enabled = !uiState.isSaving && uiState.items.isNotEmpty(),
                                isLoading = uiState.isSaving
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error banner if any
            if (uiState.errorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DangerRedLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = DangerRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = DangerRed,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = DangerRed)
                            }
                        }
                    }
                }
            }

            // Customer Selector Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_selection_card"),
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
                            Text(
                                text = "Customer Details",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                            TextButton(
                                onClick = { showCustomerPicker = true },
                                modifier = Modifier.testTag("change_customer_button")
                            ) {
                                Text(if (uiState.selectedCustomer == null) "Select Customer" else "Change")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.customerNameInput,
                                onValueChange = { viewModel.setCustomerManual(it, uiState.customerPhoneInput) },
                                label = { Text("Customer Name") },
                                placeholder = { Text("e.g. Rahul Sharma") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_name_input")
                            )
                            OutlinedTextField(
                                value = uiState.customerPhoneInput,
                                onValueChange = { input ->
                                    val digits = input.filter { it.isDigit() }.take(10)
                                    viewModel.setCustomerManual(uiState.customerNameInput, digits)
                                },
                                label = { Text("Mobile Number") },
                                placeholder = { Text("10-digit mobile number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                supportingText = {
                                    if (uiState.customerPhoneInput.isNotEmpty()) {
                                        Text("${uiState.customerPhoneInput.length}/10 digits")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("customer_phone_input")
                            )
                        }
                    }
                }
            }

            // Items Section Header & Add Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items (${uiState.items.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showCustomItemDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier.testTag("add_custom_item_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Custom Item", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { showProductPicker = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            modifier = Modifier.testTag("add_product_button")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Cart Items
            if (uiState.items.isEmpty()) {
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
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No items added yet",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap here to add products from inventory or add a custom item",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(uiState.items) { index, item ->
                    CartItemCard(
                        item = item,
                        onQtyChange = { viewModel.updateItemQuantity(index, it) },
                        onRateChange = { viewModel.updateItemRate(index, it) },
                        onDiscountChange = { viewModel.updateItemDiscount(index, it) },
                        onRemove = { viewModel.removeItem(index) }
                    )
                }
            }

            // Calculations & Discount Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Bill Summary",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyUtils.format(uiState.calculation.subtotal),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bill Level Discount Field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bill Discount (₹)", style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = if (uiState.billDiscount == 0.0) "" else uiState.billDiscount.toString(),
                                onValueChange = {
                                    val disc = it.toDoubleOrNull() ?: 0.0
                                    viewModel.setBillDiscount(disc)
                                },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(52.dp)
                                    .testTag("bill_discount_input")
                            )
                        }

                        if (uiState.calculation.taxAmount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val taxLabel = if (uiState.calculation.isInterState) "IGST" else "GST (CGST + SGST)"
                                Text(taxLabel, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    CurrencyUtils.format(uiState.calculation.taxAmount),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Grand Total",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                CurrencyUtils.format(uiState.calculation.grandTotal),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryBlue
                            )
                        }
                    }
                }
            }

            // Payment Details Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Payment Details",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Payment Shortcuts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.setFullPayment() },
                                modifier = Modifier.weight(1f).testTag("full_paid_shortcut")
                            ) {
                                Text("Full Paid (${CurrencyUtils.format(uiState.calculation.grandTotal)})")
                            }
                            OutlinedButton(
                                onClick = { viewModel.setZeroPayment() },
                                modifier = Modifier.weight(0.7f).testTag("unpaid_shortcut")
                            ) {
                                Text("Credit / ₹0")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = if (uiState.receivedAmount == 0.0) "" else uiState.receivedAmount.toString(),
                                onValueChange = {
                                    val amt = it.toDoubleOrNull() ?: 0.0
                                    viewModel.setReceivedAmount(amt)
                                },
                                label = { Text("Received Amount (₹)") },
                                placeholder = { Text("0.00") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("received_amount_input")
                            )

                            // Balance Due / Status Display
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.calculation.dueAmount > 0) DangerRed.copy(alpha = 0.08f) else SuccessGreen.copy(alpha = 0.08f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (uiState.calculation.dueAmount > 0) "Balance Due / Credit" else "Payment Status",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        color = if (uiState.calculation.dueAmount > 0) DangerRed else SuccessGreen
                                    )
                                    Text(
                                        text = if (uiState.calculation.dueAmount > 0) CurrencyUtils.format(uiState.calculation.dueAmount) else "Fully Paid ✓",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (uiState.calculation.dueAmount > 0) DangerRed else SuccessGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Payment Method", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(PaymentMethods.CASH, PaymentMethods.UPI, PaymentMethods.CARD).forEach { method ->
                                FilterChip(
                                    selected = uiState.paymentMethod == method,
                                    onClick = { viewModel.setPaymentMethod(method) },
                                    label = { Text(method) },
                                    modifier = Modifier.testTag("pay_method_${method.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.notes,
                            onValueChange = { viewModel.setNotes(it) },
                            label = { Text("Notes / Remarks (Optional)") },
                            placeholder = { Text("Add any notes, terms or delivery instructions") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // Product Picker Dialog
    if (showProductPicker) {
        ProductPickerDialog(
            products = uiState.allProducts,
            onProductSelected = {
                viewModel.addProductItem(it)
                showProductPicker = false
            },
            onDismiss = { showProductPicker = false }
        )
    }

    // Custom Item Dialog
    if (showCustomItemDialog) {
        CustomItemDialog(
            onItemAdded = { name, qty, rate, unit, tax ->
                viewModel.addCustomItem(name, qty, rate, unit, tax)
                showCustomItemDialog = false
            },
            onDismiss = { showCustomItemDialog = false }
        )
    }

    // Customer Picker Dialog
    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = uiState.allCustomers,
            onCustomerSelected = {
                viewModel.selectCustomer(it)
                showCustomerPicker = false
            },
            onDismiss = { showCustomerPicker = false }
        )
    }
}

@Composable
fun CartItemCard(
    item: BillItemInput,
    onQtyChange: (Double) -> Unit,
    onRateChange: (Double) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.productName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.productId != null) {
                        Text(
                            text = "Stock: ${item.currentStock} ${item.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.currentStock < item.quantity) DangerRed else TextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = DangerRed)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Controls row: Stepper and Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Stepper
                Column {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantLight)
                    ) {
                        IconButton(
                            onClick = { onQtyChange(item.quantity - 1.0) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${item.quantity} ${item.unit}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onQtyChange(item.quantity + 1.0) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Rate field
                OutlinedTextField(
                    value = if (item.rate == 0.0) "" else item.rate.toString(),
                    onValueChange = { onRateChange(it.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Rate (₹)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.width(130.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Item Total banner
            val calculated = item.quantity * item.rate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantLight)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.taxRate > 0) "Total (${item.taxRate}% GST incl.)" else "Item Total",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    text = CurrencyUtils.format(calculated),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
            }
        }
    }
}

@Composable
fun ProductPickerDialog(
    products: List<ProductEntity>,
    onProductSelected: (ProductEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = products.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) || it.barcode.contains(query)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .testTag("product_picker_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Product",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                AppSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Search product name or barcode..."
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.size) { i ->
                        val p = filtered[i]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductSelected(p) }
                                .testTag("picker_product_${p.name}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Stock: ${p.stockQuantity} ${p.unit} | GST: ${p.gstRate}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                Text(
                                    text = CurrencyUtils.format(p.sellingPrice),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomItemDialog(
    onItemAdded: (String, Double, Double, String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qtyText by remember { mutableStateOf("1") }
    var rateText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Pcs") }
    var taxRateText by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("custom_item_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Custom Item",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Service Fee, Custom Spare") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_item_name")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    label = { Text("Quantity") },
                    placeholder = { Text("1.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_item_qty")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (Pcs, Kg, Box, etc.)") },
                    placeholder = { Text("Pcs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("Rate (₹) *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_item_rate")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = taxRateText,
                    onValueChange = { taxRateText = it },
                    label = { Text("GST % (0, 5, 12, 18, 28)") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        text = "Add Item",
                        onClick = {
                            val qty = qtyText.toDoubleOrNull() ?: 1.0
                            val rate = rateText.toDoubleOrNull() ?: 0.0
                            val tax = taxRateText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                onItemAdded(name, qty, rate, unit, tax)
                            }
                        },
                        enabled = name.isNotBlank()
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerPickerDialog(
    customers: List<CustomerEntity>,
    onCustomerSelected: (CustomerEntity?) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = customers.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .testTag("customer_picker_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Customer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Walk-in Customer Option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCustomerSelected(null) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueLight)
                ) {
                    Text(
                        text = "👤 Walk-in Customer (Default)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = PrimaryBlue,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                AppSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Search customer name or phone..."
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered.size) { i ->
                        val c = filtered[i]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCustomerSelected(c) }
                                .testTag("picker_customer_${c.name}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = c.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                if (c.phone.isNotBlank()) {
                                    Text(
                                        text = "Phone: ${c.phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
