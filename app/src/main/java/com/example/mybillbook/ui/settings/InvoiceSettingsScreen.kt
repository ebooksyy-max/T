package com.example.mybillbook.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.InvoiceSettingsEntity
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.theme.PrimaryBlue
import com.example.mybillbook.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun InvoiceSettingsScreen(
    container: AppContainer,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var prefix by remember { mutableStateOf("INV-") }
    var nextNumber by remember { mutableStateOf("1") }
    var taxInclusive by remember { mutableStateOf(false) }
    var roundOff by remember { mutableStateOf(true) }
    var terms by remember { mutableStateOf("") }
    var footer by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val s = container.businessRepository.getInvoiceSettingsDirect()
        prefix = s.invoicePrefix
        nextNumber = s.nextInvoiceNumber.toString()
        taxInclusive = s.taxInclusive
        roundOff = s.roundOffTotal
        terms = s.termsAndConditions
        footer = s.footerText
    }

    Scaffold(
        modifier = modifier.testTag("invoice_settings_screen"),
        topBar = {
            AppTopBar(
                title = "Invoice & Printing Setup",
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
                        text = "Save Invoice Preferences",
                        onClick = {
                            isSaving = true
                            scope.launch {
                                val updated = InvoiceSettingsEntity(
                                    id = 1L,
                                    invoicePrefix = prefix.trim(),
                                    nextInvoiceNumber = nextNumber.toLongOrNull() ?: 1L,
                                    taxInclusive = taxInclusive,
                                    roundOffTotal = roundOff,
                                    termsAndConditions = terms.trim(),
                                    footerText = footer.trim()
                                )
                                container.businessRepository.updateInvoiceSettings(updated)
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
            Text("Invoice Numbering", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)

            OutlinedTextField(
                value = prefix,
                onValueChange = { prefix = it },
                label = { Text("Invoice Prefix") },
                placeholder = { Text("e.g. INV-") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("invoice_prefix_input")
            )

            OutlinedTextField(
                value = nextNumber,
                onValueChange = { nextNumber = it },
                label = { Text("Next Serial Number") },
                placeholder = { Text("e.g. 1001") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("next_invoice_number_input")
            )

            Text("Tax & Price Calculations", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tax Inclusive Pricing", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Product selling price already includes GST", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Switch(
                    checked = taxInclusive,
                    onCheckedChange = { taxInclusive = it }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto Round-Off Grand Total", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text("Rounds bill total to the nearest whole rupee", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Switch(
                    checked = roundOff,
                    onCheckedChange = { roundOff = it }
                )
            }

            Text("Printable Terms & Footer", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = PrimaryBlue)

            OutlinedTextField(
                value = terms,
                onValueChange = { terms = it },
                label = { Text("Terms & Conditions") },
                placeholder = { Text("Enter terms, payment instructions, return policy...") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = footer,
                onValueChange = { footer = it },
                label = { Text("Invoice Footer Greeting") },
                placeholder = { Text("e.g. Thank you for your business!") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
