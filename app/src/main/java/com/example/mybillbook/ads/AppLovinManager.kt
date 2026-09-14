package com.example.mybillbook.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkInitializationConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLovinManager {
    private const val TAG = "AppLovinManager"

    private val _isSdkInitialized = MutableStateFlow(false)
    val isSdkInitialized: StateFlow<Boolean> = _isSdkInitialized.asStateFlow()

    private var interstitialAd: MaxInterstitialAd? = null
    private var isInterstitialLoading = false
    private var onDismissCallback: (() -> Unit)? = null

    fun initialize(context: Context, onComplete: (() -> Unit)? = null) {
        if (_isSdkInitialized.value) {
            onComplete?.invoke()
            return
        }

        if (AppLovinConfig.isUsingPlaceholder(context)) {
            Log.d(TAG, "Using AppLovin Test Mode. Real SDK network initialization deferred until real SDK key is configured.")
            _isSdkInitialized.value = true
            onComplete?.invoke()
            return
        }

        val sdkKey = AppLovinConfig.getSdkKey(context)
        try {
            val initConfig = AppLovinSdkInitializationConfiguration.builder(sdkKey, context)
                .setMediationProvider(AppLovinMediationProvider.MAX)
                .build()

            AppLovinSdk.getInstance(context).initialize(initConfig) { sdkConfig ->
                Log.d(TAG, "AppLovin SDK initialized successfully.")
                _isSdkInitialized.value = true
                onComplete?.invoke()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize AppLovin SDK", e)
            _isSdkInitialized.value = false
            onComplete?.invoke()
        }
    }

    fun loadInterstitial(activity: Activity) {
        val context = activity.applicationContext
        if (!AppLovinConfig.isAdsEnabled(context) || AppLovinConfig.isUsingPlaceholder(context)) return

        val adUnitId = AppLovinConfig.getInterstitialAdUnitId(context)
        if (adUnitId.isBlank() || isInterstitialLoading) return

        try {
            if (interstitialAd == null) {
                interstitialAd = MaxInterstitialAd(adUnitId, activity).apply {
                    setListener(object : MaxAdListener {
                        override fun onAdLoaded(ad: MaxAd) {
                            Log.d(TAG, "Interstitial ad loaded: ${ad.adUnitId}")
                            isInterstitialLoading = false
                        }

                        override fun onAdDisplayed(ad: MaxAd) {
                            Log.d(TAG, "Interstitial ad displayed")
                        }

                        override fun onAdHidden(ad: MaxAd) {
                            Log.d(TAG, "Interstitial ad dismissed")
                            onDismissCallback?.invoke()
                            onDismissCallback = null
                            // Preload next interstitial
                            loadInterstitial(activity)
                        }

                        override fun onAdClicked(ad: MaxAd) {
                            Log.d(TAG, "Interstitial ad clicked")
                        }

                        override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                            Log.w(TAG, "Interstitial failed to load: ${error.message} (code: ${error.code})")
                            isInterstitialLoading = false
                        }

                        override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                            Log.w(TAG, "Interstitial failed to display: ${error.message} (code: ${error.code})")
                            onDismissCallback?.invoke()
                            onDismissCallback = null
                            loadInterstitial(activity)
                        }
                    })
                }
            }

            isInterstitialLoading = true
            interstitialAd?.loadAd()
        } catch (e: Throwable) {
            Log.e(TAG, "Exception loading interstitial", e)
            isInterstitialLoading = false
        }
    }

    fun showInterstitial(activity: Activity, onClosed: () -> Unit) {
        val context = activity.applicationContext
        if (!AppLovinConfig.isAdsEnabled(context)) {
            onClosed()
            return
        }

        val ad = interstitialAd
        if (ad != null && ad.isReady) {
            onDismissCallback = onClosed
            ad.showAd(activity)
        } else {
            // Not ready yet, invoke callback immediately without blocking user flow
            onClosed()
            loadInterstitial(activity)
        }
    }

    fun isInterstitialReady(): Boolean {
        return interstitialAd?.isReady == true
    }
}
