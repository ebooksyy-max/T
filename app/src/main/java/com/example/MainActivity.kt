package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mybillbook.ads.AppLovinManager
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.ui.navigation.AppNavigation
import com.example.mybillbook.ui.theme.BillBookColors
import com.example.mybillbook.ui.theme.MyBillBookTheme
import com.example.mybillbook.worker.ReminderWorker

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Native slim status bar with exact background color #F7F9FC & dark icons
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.statusBars())
        insetsController.isAppearanceLightStatusBars = true
        insetsController.isAppearanceLightNavigationBars = true
        window.statusBarColor = Color.parseColor("#F7F9FC")
        window.navigationBarColor = Color.parseColor("#FFFFFF")

        appContainer = AppContainer(applicationContext)

        // Initialize AppLovin MAX monetization SDK
        AppLovinManager.initialize(applicationContext) {
            AppLovinManager.loadInterstitial(this@MainActivity)
        }

        // Schedule periodic local background reminders for low stock and overdue bills
        try {
            ReminderWorker.schedulePeriodicReminders(applicationContext)
        } catch (e: Exception) {
            // Log or ignore gracefully
        }

        setContent {
            MyBillBookTheme(darkTheme = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BillBookColors.Background
                ) {
                    AppNavigation(container = appContainer)
                }
            }
        }
    }
}
