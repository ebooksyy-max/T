package com.example.mybillbook.ui.khata

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.mybillbook.ui.bills.CustomerPickerDialog
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.*
import com.example.mybillbook.utils.CurrencyUtils
import kotlinx.coroutines.launch

@Composable
fun RecordPaymentScreen(
    initialPartyType: String = "CUSTOMER",
    initialPartyId: Long? = null,
    initialInvoiceId: Long? = null,
    container: AppContainer,
    onBackClick: () -> Unit,
    onPaymentRecorded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var partyType by remember { mutableStateOf(initialPartyType) }
    var selectedCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var allCustomers by remember { mutableStateOf<List<CustomerEntity>>(emptyList()) }
    var showCustomerPicker by remember { mutableStateOf(false) }

    var paymentType by remember { mutableStateOf(if (partyType == "CUSTOMER") "PAYMENT_IN" else "PAYMENT_OUT") }
    var amountText by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethods.CASH) }
    var referenceNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var currentBalance by remember { mutableStateOf(0.0) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        container.customerRepository.allCustomers.collect { list ->
            allCustomers = list
            if (initialPartyId != null && initialPartyId > 0 && selectedCustomer == null) {
                val found = list.find { it.id == initialPartyId }
                if (found != null) {
                    selectedCustomer = found
                    val bal = container.customerRepository.getCustomerBalanceSummary(found.id)
                    currentBalance = bal.netBalance
                }
            }
        }
    }

    LaunchedEffect(selectedCustomer) {
        val c = selectedCustomer
        if (c != null) {
            val bal = container.customerRepository.getCustomerBalanceSummary(c.id)
            currentBalance = bal.netBalance
        } else {
            currentBalance = 0.0
        }
    }

    Scaffold(
        modifier = modifier.testTag("record_payment_screen"),
        topBar = {
            AppTopBar(
                title = "Record Payment",
                subtitle = if (paymentType == "PAYMENT_IN") "Payment In (Received)" else "Payment Out (Paid)",
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
                        text = "Save Payment",
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 0.0
                            if (amt <= 0.0) {
                                errorMessage = "Please enter a valid amount greater than 0"
                                return@PrimaryButton
                            }
                            if (selectedCustomer == null) {
                                errorMessage = "Please select a customer"
                                return@PrimaryButton
                            }

                            isSaving = true
                            scope.launch {
                                val noteWithRef = if (referenceNumber.isNotBlank()) "${notes.trim()} (Ref: ${referenceNumber.trim()})" else notes.trim()
                                container.paymentRepository.recordCustomerPayment(
                                    customerId = selectedCustomer!!.id,
                                    amount = amt,
                                    paymentMethod = paymentMethod,
                                    note = noteWithRef,
                                    invoiceId = if (initialInvoiceId != null && initialInvoiceId > 0) initialInvoiceId else null
                                )
                                isSaving = false
                                onPaymentRecorded()
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DangerRedLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Payment Type Toggle (Payment In vs Payment Out)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = paymentType == "PAYMENT_IN",
                    onClick = { paymentType = "PAYMENT_IN" },
                    label = { Text("Payment Received (In)") },
                    modifier = Modifier.weight(1f).testTag("payment_in_chip")
                )
                FilterChip(
                    selected = paymentType == "PAYMENT_OUT",
                    onClick = { paymentType = "PAYMENT_OUT" },
                    label = { Text("Payment Paid (Out)") },
                    modifier = Modifier.weight(1f).testTag("payment_out_chip")
                )
            }

            // Customer Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Party / Customer",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryBlue
                        )
                        TextButton(
                            onClick = { showCustomerPicker = true },
                            modifier = Modifier.testTag("select_party_button")
                        ) {
                            Text(if (selectedCustomer == null) "Select Customer" else "Change")
                        }
                    }

                    if (selectedCustomer != null) {
                        Text(
                            text = selectedCustomer!!.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedCustomer!!.phone.isNotBlank()) {
                            Text(
                                text = "Phone: ${selectedCustomer!!.phone}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Due Balance:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = CurrencyUtils.format(currentBalance),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (currentBalance > 0) DangerRed else SuccessGreen
                            )
                        }
                    } else {
                        Text(
                            text = "Tap 'Select Customer' to choose whose payment you are recording.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Amount Input
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorMessage = null
                },
                label = { Text("Payment Amount (₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
            )

            // Quick Fill Balance Shortcut
            if (currentBalance > 0) {
                OutlinedButton(
                    onClick = { amountText = currentBalance.toString() },
                    modifier = Modifier.fillMaxWidth().testTag("pay_full_due_button")
                ) {
                    Text("Fill Full Due Amount (${CurrencyUtils.format(currentBalance)})")
                }
            }

            // Payment Mode
            Text("Payment Method", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(PaymentMethods.CASH, PaymentMethods.UPI, PaymentMethods.BANK, PaymentMethods.CARD).forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method) },
                        modifier = Modifier.testTag("pay_method_${method.lowercase()}")
                    )
                }
            }

            // Reference Number
            OutlinedTextField(
                value = referenceNumber,
                onValueChange = { referenceNumber = it },
                label = { Text("Reference / UPI / Txn ID (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Remarks (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = allCustomers,
            onCustomerSelected = {
                selectedCustomer = it
                showCustomerPicker = false
            },
            onDismiss = { showCustomerPicker = false }
        )
    }
}
