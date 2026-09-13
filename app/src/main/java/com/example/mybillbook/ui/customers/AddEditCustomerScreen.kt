package com.example.mybillbook.ui.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.CustomerEntity
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import kotlinx.coroutines.launch

@Composable
fun AddEditCustomerScreen(
    customerId: Long?,
    container: AppContainer,
    onBackClick: () -> Unit,
    onCustomerSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var openingBalance by remember { mutableStateOf("0") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    LaunchedEffect(customerId) {
        if (customerId != null && customerId > 0) {
            val existing = container.customerRepository.getCustomerByIdDirect(customerId)
            if (existing != null) {
                name = existing.name
                phone = existing.phone
                email = existing.email
                address = existing.address
                city = existing.city
                state = existing.state
                pincode = existing.pincode
                gstin = existing.gstin
                openingBalance = existing.openingBalance.toString()
                notes = existing.notes
            }
        }
    }

    Scaffold(
        modifier = modifier.testTag("add_edit_customer_screen"),
        topBar = {
            AppTopBar(
                title = if (customerId != null && customerId > 0) "Edit Customer" else "Add Customer",
                onBackClick = onBackClick
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
                        text = if (customerId != null && customerId > 0) "Update Customer" else "Save Customer",
                        onClick = {
                            if (name.isBlank()) {
                                nameError = true
                                return@PrimaryButton
                            }
                            isSaving = true
                            scope.launch {
                                val customer = CustomerEntity(
                                    id = customerId ?: 0L,
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    address = address.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    pincode = pincode.trim(),
                                    gstin = gstin.trim().uppercase(),
                                    openingBalance = openingBalance.toDoubleOrNull() ?: 0.0,
                                    notes = notes.trim()
                                )
                                val savedId = if (customerId != null && customerId > 0) {
                                    container.customerRepository.updateCustomer(customer)
                                    customerId
                                } else {
                                    container.customerRepository.insertCustomer(customer)
                                }
                                isSaving = false
                                onCustomerSaved(savedId)
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
                label = { Text("Customer Name *") },
                isError = nameError,
                supportingText = { if (nameError) Text("Customer name is required") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("customer_name_field")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("customer_phone_field")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = it },
                label = { Text("GSTIN (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Billing Address") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text("Pincode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text("Opening Balance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Remarks") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
