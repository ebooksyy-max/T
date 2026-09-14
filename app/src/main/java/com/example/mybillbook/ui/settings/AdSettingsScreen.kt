package com.example.mybillbook.ui.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mybillbook.ads.AppLovinBanner
import com.example.mybillbook.ads.AppLovinConfig
import com.example.mybillbook.ads.AppLovinManager
import com.example.mybillbook.ui.components.AppTopBar
import com.example.mybillbook.ui.components.PrimaryButton
import com.example.mybillbook.ui.components.SecondaryButton
import com.example.mybillbook.ui.theme.DangerRed
import com.example.mybillbook.ui.theme.PrimaryBlue
import com.example.mybillbook.ui.theme.SuccessGreen
import com.example.mybillbook.ui.theme.TextMuted

@Composable
fun AdSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var sdkKey by remember { mutableStateOf(AppLovinConfig.getSdkKey(context)) }
    var bannerId by remember { mutableStateOf(AppLovinConfig.getBannerAdUnitId(context)) }
    var interstitialId by remember { mutableStateOf(AppLovinConfig.getInterstitialAdUnitId(context)) }
    var adsEnabled by remember { mutableStateOf(AppLovinConfig.isAdsEnabled(context)) }

    val isInitialized by AppLovinManager.isSdkInitialized.collectAsState()
    val isPlaceholder = AppLovinConfig.isUsingPlaceholder(context)

    Scaffold(
        modifier = modifier.testTag("ad_settings_screen"),
        topBar = {
            AppTopBar(
                title = "AppLovin MAX Monetization",
                onBackClick = onBackClick
            )
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
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isInitialized) SuccessGreen.copy(alpha = 0.15f) else PrimaryBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInitialized) Icons.Default.CheckCircle else Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = if (isInitialized) SuccessGreen else PrimaryBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isInitialized) "AppLovin SDK Active" else "Ready (Test Mode)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPlaceholder) "Using Test IDs • Safe for testing" else "Live Ad Unit IDs Configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = adsEnabled,
                        onCheckedChange = {
                            adsEnabled = it
                            AppLovinConfig.setAdsEnabled(context, it)
                            Toast.makeText(context, if (it) "Ads Enabled" else "Ads Disabled", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // Live Ad Banner Preview Section
            Text(
                text = "Banner Ad Preview",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TextMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    AppLovinBanner(
                        modifier = Modifier.fillMaxWidth(),
                        adUnitId = bannerId
                    )
                }
            }

            // Interstitial Ad Testing Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Interstitial Ad (Full Screen)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text(
                        text = "Triggered automatically when a bill/invoice is saved, or test it directly here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    SecondaryButton(
                        text = "Show Test Interstitial Now",
                        icon = Icons.Default.PlayArrow,
                        onClick = {
                            if (activity != null) {
                                if (AppLovinConfig.isUsingPlaceholder(context)) {
                                    Toast.makeText(context, "Test Interstitial ad triggered & dismissed (Test Mode)", Toast.LENGTH_SHORT).show()
                                } else if (AppLovinManager.isInterstitialReady()) {
                                    AppLovinManager.showInterstitial(activity) {
                                        Toast.makeText(context, "Interstitial ad dismissed", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Loading live interstitial ad... Preload requested", Toast.LENGTH_SHORT).show()
                                    AppLovinManager.loadInterstitial(activity)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Credentials & Ad Unit IDs Configuration
            Text(
                text = "AppLovin Credentials & Ad Units",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = TextMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = sdkKey,
                        onValueChange = { sdkKey = it },
                        label = { Text("AppLovin SDK Key") },
                        placeholder = { Text("Your AppLovin SDK Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bannerId,
                        onValueChange = { bannerId = it },
                        label = { Text("Banner Ad Unit ID") },
                        placeholder = { Text("e.g. 1234567890abcdef") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = interstitialId,
                        onValueChange = { interstitialId = it },
                        label = { Text("Interstitial Ad Unit ID") },
                        placeholder = { Text("e.g. abcdef1234567890") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                sdkKey = AppLovinConfig.DEFAULT_SDK_KEY
                                bannerId = AppLovinConfig.DEFAULT_BANNER_ID
                                interstitialId = AppLovinConfig.DEFAULT_INTERSTITIAL_ID
                                AppLovinConfig.setSdkKey(context, sdkKey)
                                AppLovinConfig.setBannerAdUnitId(context, bannerId)
                                AppLovinConfig.setInterstitialAdUnitId(context, interstitialId)
                                Toast.makeText(context, "Reset to default test IDs", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset Test IDs")
                        }

                        PrimaryButton(
                            text = "Save Keys",
                            onClick = {
                                AppLovinConfig.setSdkKey(context, sdkKey)
                                AppLovinConfig.setBannerAdUnitId(context, bannerId)
                                AppLovinConfig.setInterstitialAdUnitId(context, interstitialId)
                                if (activity != null) {
                                    AppLovinManager.initialize(context) {
                                        AppLovinManager.loadInterstitial(activity)
                                    }
                                }
                                Toast.makeText(context, "AppLovin ad settings saved successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
