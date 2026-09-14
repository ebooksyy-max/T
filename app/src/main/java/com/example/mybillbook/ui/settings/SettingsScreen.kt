package com.example.mybillbook.ui.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.SampleDataLoader
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.ConfirmDialog
import com.example.mybillbook.ui.theme.DangerRed
import com.example.mybillbook.ui.theme.PrimaryBlue
import com.example.mybillbook.ui.theme.TextMuted
import com.example.mybillbook.utils.BackupManager
import com.example.mybillbook.utils.ShareUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    container: AppContainer,
    onBackClick: () -> Unit,
    onBusinessProfileClick: () -> Unit,
    onInvoiceSettingsClick: () -> Unit,
    onAdSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var business by remember { mutableStateOf<BusinessEntity?>(null) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showSampleDataDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        business = container.businessRepository.getBusinessDirect()
    }

    Scaffold(
        modifier = modifier.testTag("settings_screen"),
        topBar = {
            AppTopBar(
                title = "Settings & Business Setup",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Business Profile Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBusinessProfileClick() }
                        .testTag("business_profile_settings_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = business?.businessName?.ifBlank { "Set Business Profile" } ?: "My Business",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Name, Phone, GSTIN, UPI ID & Bank details",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
            }

            // Setting Groups
            item {
                Text(
                    text = "Application Preferences",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryBlue
                )
            }

            item {
                SettingsNavigationItem(
                    icon = Icons.Default.Receipt,
                    title = "Invoice & Printing Setup",
                    subtitle = "Prefix, serial numbering, terms & conditions",
                    onClick = onInvoiceSettingsClick
                )
            }

            item {
                SettingsNavigationItem(
                    icon = Icons.Default.MonetizationOn,
                    title = "AppLovin Monetization & Ads",
                    subtitle = "Banner & Interstitial test IDs, SDK keys, ad controls",
                    onClick = onAdSettingsClick
                )
            }

            item {
                SettingsNavigationItem(
                    icon = Icons.Default.CloudDownload,
                    title = "Backup & Export Data",
                    subtitle = "Export complete business database to JSON backup file",
                    onClick = {
                        scope.launch {
                            val backupFile = BackupManager.exportBackup(context, container.database)
                            ShareUtils.shareFile(context, backupFile, "application/zip", "My Bill Book Backup")
                        }
                    }
                )
            }

            item {
                SettingsNavigationItem(
                    icon = Icons.Default.AutoFixHigh,
                    title = "Load Realistic Demo Data",
                    subtitle = "Populates store products, sample customers & bills",
                    onClick = { showSampleDataDialog = true }
                )
            }

            // App Version Info
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "My Bill Book v1.0.0",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "100% Offline-First Native Android Business App",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        }
    }

    if (showSampleDataDialog) {
        ConfirmDialog(
            title = "Load Demo Data?",
            message = "This will load sample electronics, stationery products, customers, and invoices so you can test all features instantly. Continue?",
            confirmText = "Load Data",
            onConfirm = {
                showSampleDataDialog = false
                scope.launch {
                    SampleDataLoader.loadRealisticSampleData(container)
                    business = container.businessRepository.getBusinessDirect()
                }
            },
            onDismiss = { showSampleDataDialog = false }
        )
    }
}

@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("settings_item_${title.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
        }
    }
}
