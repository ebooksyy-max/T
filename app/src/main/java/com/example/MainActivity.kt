package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.ui.navigation.AppNavigation
import com.example.mybillbook.ui.theme.MyBillBookTheme
import com.example.mybillbook.worker.ReminderWorker

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(applicationContext)

        // Schedule periodic local background reminders for low stock and overdue bills
        try {
            ReminderWorker.schedulePeriodicReminders(applicationContext)
        } catch (e: Exception) {
            // Log or ignore gracefully
        }

        setContent {
            MyBillBookTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(container = appContainer)
                }
            }
        }
    }
}

