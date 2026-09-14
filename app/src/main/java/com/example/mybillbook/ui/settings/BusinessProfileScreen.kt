package com.example.mybillbook.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@Composable
fun BusinessProfileScreen(
    container: AppContainer,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val b = container.businessRepository.getBusinessDirect()
        businessName = b.businessName
        ownerName = b.ownerName
        phone = b.phone
        email = b.email
        address = b.address
        city = b.city
        state = b.state
        pincode = b.pincode
        gstin = b.gstin
        pan = b.pan
        upiId = b.upiId
        bankName = b.bankName
        accountNumber = b.accountNumber
        ifsc = b.ifsc
    }

    Scaffold(
        modifier = modifier.testTag("business_profile_screen"),
        topBar = {
            AppTopBar(
                title = "Business Profile",
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
                        text = "Save Profile",
                        onClick = {
                            if (phone.isNotBlank() && phone.length != 10) {
                                phoneError = true
                                return@PrimaryButton
                            }
                            isSaving = true
                            scope.launch {
                                val updated = BusinessEntity(
                                    id = 1L,
                                    businessName = businessName.trim(),
                                    ownerName = ownerName.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    address = address.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    pincode = pincode.trim(),
                                    gstin = gstin.trim().uppercase(),
                                    pan = pan.trim().uppercase(),
                                    upiId = upiId.trim(),
                                    bankName = bankName.trim(),
                                    accountNumber = accountNumber.trim(),
                                    ifsc = ifsc.trim().uppercase()
                                )
                                container.businessRepository.updateBusiness(updated)
                                isSaving = false
                                onBackClick()
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
            Text("General Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business / Store Name *") },
                placeholder = { Text("e.g. Sharma General Store") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("profile_business_name")
            )

            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner Name") },
                placeholder = { Text("e.g. Rajesh Kumar") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(10)
                    phone = digits
                    if (phoneError && (digits.isEmpty() || digits.length == 10)) {
                        phoneError = false
                    }
                },
                label = { Text("Contact Mobile Number") },
                placeholder = { Text("10-digit mobile number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = phoneError,
                supportingText = {
                    if (phoneError) {
                        Text("Please enter a valid 10-digit mobile number", color = MaterialTheme.colorScheme.error)
                    } else if (phone.isNotEmpty()) {
                        Text("${phone.length}/10 digits")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Business Email") },
                placeholder = { Text("e.g. store@example.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Shop / Office Address") },
                placeholder = { Text("Shop no., building, street address") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                placeholder = { Text("e.g. Mumbai, Bengaluru, Delhi") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                placeholder = { Text("e.g. Maharashtra, Karnataka") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Taxation (GST / PAN)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)

            OutlinedTextField(
                value = gstin,
                onValueChange = { gstin = it },
                label = { Text("GSTIN") },
                placeholder = { Text("e.g. 27AAAAA0000A1Z5") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = pan,
                onValueChange = { pan = it },
                label = { Text("PAN Number") },
                placeholder = { Text("e.g. ABCDE1234F") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Digital Payments & Bank Details", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)

            OutlinedTextField(
                value = upiId,
                onValueChange = { upiId = it },
                label = { Text("UPI ID") },
                placeholder = { Text("e.g. yourstore@okhdfcbank") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("profile_upi_id")
            )

            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name") },
                placeholder = { Text("e.g. HDFC Bank, SBI, ICICI") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ifsc,
                onValueChange = { ifsc = it },
                label = { Text("IFSC Code") },
                placeholder = { Text("e.g. HDFC0001234") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = accountNumber,
                onValueChange = { accountNumber = it },
                label = { Text("Bank Account Number") },
                placeholder = { Text("e.g. 50100234567890") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
