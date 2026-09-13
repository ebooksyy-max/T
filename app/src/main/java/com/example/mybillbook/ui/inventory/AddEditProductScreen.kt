package com.example.mybillbook.ui.inventory

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.CategoryEntity
import com.example.mybillbook.data.entity.ProductEntity
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.ConfirmDialog
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.DangerRed
import kotlinx.coroutines.launch

@Composable
fun AddEditProductScreen(
    productId: Long?,
    container: AppContainer,
    onBackClick: () -> Unit,
    onProductSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Long?>(null) }
    var unit by remember { mutableStateOf("Pcs") }
    var purchasePrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var gstRate by remember { mutableStateOf(18.0) }
    var stockQuantity by remember { mutableStateOf("0") }
    var minimumStock by remember { mutableStateOf("5") }
    var description by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf<List<CategoryEntity>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        categories = container.productRepository.getAllCategoriesDirect()
        if (productId != null && productId > 0) {
            val p = container.productRepository.getProductByIdDirect(productId)
            if (p != null) {
                name = p.name
                sku = p.sku
                barcode = p.barcode
                selectedCategory = p.categoryId
                unit = p.unit
                purchasePrice = p.purchasePrice.toString()
                sellingPrice = p.sellingPrice.toString()
                gstRate = p.gstRate
                stockQuantity = p.stockQuantity.toString()
                minimumStock = p.minimumStock.toString()
                description = p.description
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("add_edit_product_screen"),
        topBar = {
            AppTopBar(
                title = if (productId != null && productId > 0) "Edit Product" else "Add Product",
                onBackClick = onBackClick,
                actions = {
                    if (productId != null && productId > 0) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("delete_product_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = DangerRed)
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    PrimaryButton(
                        text = if (productId != null && productId > 0) "Update Product" else "Save Product",
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@PrimaryButton
                            }
                            isSaving = true
                            scope.launch {
                                val product = ProductEntity(
                                    id = productId ?: 0L,
                                    name = name.trim(),
                                    sku = sku.trim(),
                                    barcode = barcode.trim(),
                                    categoryId = selectedCategory,
                                    unit = unit.trim(),
                                    purchasePrice = purchasePrice.toDoubleOrNull() ?: 0.0,
                                    sellingPrice = sellingPrice.toDoubleOrNull() ?: 0.0,
                                    gstRate = gstRate,
                                    stockQuantity = stockQuantity.toDoubleOrNull() ?: 0.0,
                                    minimumStock = minimumStock.toDoubleOrNull() ?: 5.0,
                                    description = description.trim()
                                )
                                val savedId = if (productId != null && productId > 0) {
                                    container.productRepository.updateProduct(product)
                                    productId
                                } else {
                                    container.productRepository.insertProduct(product)
                                }
                                isSaving = false
                                onProductSaved(savedId)
                            }
                        },
                        isLoading = isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Product / Item Name *") },
                isError = nameError,
                supportingText = { if (nameError) Text("Item name is required") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("product_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU / Item Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("Barcode") },
                    trailingIcon = {
                        IconButton(onClick = {
                            barcode = (100000000000L..999999999999L).random().toString()
                        }) {
                            Icon(Icons.Default.QrCode, contentDescription = "Generate Barcode")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Unit Selector Chips
            Text("Unit", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            val units = listOf("Pcs", "Kg", "Gram", "Liter", "Box", "Pack", "Meter", "Dozen")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                units.forEach { u ->
                    FilterChip(
                        selected = unit == u,
                        onClick = { unit = u },
                        label = { Text(u) },
                        modifier = Modifier.testTag("unit_chip_$u")
                    )
                }
            }

            // Pricing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("selling_price_input")
                )
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Purchase Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // GST Rate Chips
            Text("GST Tax Rate (%)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            val taxRates = listOf(0.0, 5.0, 12.0, 18.0, 28.0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                taxRates.forEach { rate ->
                    FilterChip(
                        selected = gstRate == rate,
                        onClick = { gstRate = rate },
                        label = { Text("${rate.toInt()}%") },
                        modifier = Modifier.testTag("gst_chip_${rate.toInt()}")
                    )
                }
            }

            // Stock Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = { stockQuantity = it },
                    label = { Text("Initial Stock Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("initial_stock_input")
                )
                OutlinedTextField(
                    value = minimumStock,
                    onValueChange = { minimumStock = it },
                    label = { Text("Low Stock Alert Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Item Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showDeleteDialog && productId != null) {
        ConfirmDialog(
            title = "Delete Product?",
            message = "Are you sure you want to delete '$name'? This item will no longer appear in inventory or billing.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    container.productRepository.deleteProduct(productId)
                    onBackClick()
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
