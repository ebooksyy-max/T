package com.example.mybillbook.ads

import android.content.Context
import android.content.SharedPreferences

object AppLovinConfig {
    private const val PREFS_NAME = "applovin_ad_config"

    private const val KEY_SDK_KEY = "sdk_key"
    private const val KEY_BANNER_AD_UNIT_ID = "banner_ad_unit_id"
    private const val KEY_INTERSTITIAL_AD_UNIT_ID = "interstitial_ad_unit_id"
    private const val KEY_ADS_ENABLED = "ads_enabled"
    private const val KEY_TEST_MODE = "is_test_mode"

    // Default placeholder / test IDs
    const val DEFAULT_SDK_KEY = "APPLOVIN_SDK_KEY_PLACEHOLDER"
    const val DEFAULT_BANNER_ID = "TEST_BANNER_AD_UNIT"
    const val DEFAULT_INTERSTITIAL_ID = "TEST_INTERSTITIAL_AD_UNIT"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSdkKey(context: Context): String {
        return getPrefs(context).getString(KEY_SDK_KEY, DEFAULT_SDK_KEY) ?: DEFAULT_SDK_KEY
    }

    fun setSdkKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_SDK_KEY, key.trim()).apply()
    }

    fun getBannerAdUnitId(context: Context): String {
        return getPrefs(context).getString(KEY_BANNER_AD_UNIT_ID, DEFAULT_BANNER_ID) ?: DEFAULT_BANNER_ID
    }

    fun setBannerAdUnitId(context: Context, id: String) {
        getPrefs(context).edit().putString(KEY_BANNER_AD_UNIT_ID, id.trim()).apply()
    }

    fun getInterstitialAdUnitId(context: Context): String {
        return getPrefs(context).getString(KEY_INTERSTITIAL_AD_UNIT_ID, DEFAULT_INTERSTITIAL_ID) ?: DEFAULT_INTERSTITIAL_ID
    }

    fun setInterstitialAdUnitId(context: Context, id: String) {
        getPrefs(context).edit().putString(KEY_INTERSTITIAL_AD_UNIT_ID, id.trim()).apply()
    }

    fun isAdsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ADS_ENABLED, true)
    }

    fun setAdsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ADS_ENABLED, enabled).apply()
    }

    fun isTestMode(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TEST_MODE, true)
    }

    fun setTestMode(context: Context, testMode: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TEST_MODE, testMode).apply()
    }

    fun isUsingPlaceholder(context: Context): Boolean {
        val key = getSdkKey(context)
        return key.isBlank() || key == DEFAULT_SDK_KEY
    }
}
