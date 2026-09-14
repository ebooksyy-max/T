package com.example.mybillbook.ads

import android.content.Context
import android.graphics.Color as AndroidColor
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.viewinterop.AndroidView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import com.example.mybillbook.ui.theme.PrimaryBlue
import com.example.mybillbook.ui.theme.TextMuted

private const val TAG = "AppLovinBanner"

@Composable
fun AppLovinBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = ""
) {
    val context = LocalContext.current
    val adsEnabled = remember { AppLovinConfig.isAdsEnabled(context) }
    if (!adsEnabled) return

    val resolvedAdUnitId = remember(adUnitId) {
        if (adUnitId.isNotBlank()) adUnitId else AppLovinConfig.getBannerAdUnitId(context)
    }

    var isAdLoaded by remember { mutableStateOf(false) }
    var adLoadError by remember { mutableStateOf<String?>(null) }
    val isPlaceholder = remember { AppLovinConfig.isUsingPlaceholder(context) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("applovin_banner_container"),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaceholder) {
            // Test Mode Preview Banner only in DEBUG mode
            if (com.example.BuildConfig.DEBUG) {
                TestAdBannerPreview(adUnitId = resolvedAdUnitId)
            }
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { ctx ->
                    val frameLayout = FrameLayout(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            AppLovinSdkUtils.dpToPx(ctx, 50)
                        )
                    }

                    try {
                        val maxAdView = MaxAdView(resolvedAdUnitId, ctx).apply {
                            setListener(object : MaxAdViewAdListener {
                                override fun onAdLoaded(ad: MaxAd) {
                                    Log.d(TAG, "Banner ad loaded: ${ad.adUnitId}")
                                    isAdLoaded = true
                                    adLoadError = null
                                }

                                override fun onAdDisplayed(ad: MaxAd) {}
                                override fun onAdHidden(ad: MaxAd) {}
                                override fun onAdClicked(ad: MaxAd) {}

                                override fun onAdLoadFailed(unitId: String, error: MaxError) {
                                    Log.w(TAG, "Banner load failed: ${error.message}")
                                    isAdLoaded = false
                                    adLoadError = error.message
                                }

                                override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                                    Log.w(TAG, "Banner display failed: ${error.message}")
                                }

                                override fun onAdExpanded(ad: MaxAd) {}
                                override fun onAdCollapsed(ad: MaxAd) {}
                            })

                            // Set background to transparent or light
                            setBackgroundColor(AndroidColor.TRANSPARENT)
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                AppLovinSdkUtils.dpToPx(ctx, 50)
                            )
                        }

                        maxAdView.loadAd()
                        frameLayout.addView(maxAdView)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Failed creating MaxAdView", e)
                        adLoadError = e.message
                    }

                    frameLayout
                },
                update = {
                    // Update ad view if needed
                }
            )

            if (!isAdLoaded && adLoadError != null) {
                // Graceful fallback display during development
                TestAdBannerPreview(adUnitId = resolvedAdUnitId, note = "Test Mode Active")
            }
        }
    }
}

@Composable
fun TestAdBannerPreview(
    adUnitId: String,
    note: String = "AppLovin MAX Test Ad Banner"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(PrimaryBlue.copy(alpha = 0.08f))
            .border(1.dp, PrimaryBlue.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryBlue)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AD",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$note • $adUnitId",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = PrimaryBlue,
                maxLines = 1
            )
        }
    }
}
