package com.example.mybillbook.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mybillbook.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "my_bill_book_reminders"
        const val CHANNEL_NAME = "Stock & Payment Reminders"
        const val NOTIFICATION_ID_STOCK = 1001
        const val NOTIFICATION_ID_DUE = 1002

        fun schedulePeriodicReminders(context: Context) {
            val request = androidx.work.PeriodicWorkRequestBuilder<ReminderWorker>(6, java.util.concurrent.TimeUnit.HOURS)
                .build()
            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "business_reminders",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)
            val settings = database.settingsDao().getAppSettingsDirect()

            createNotificationChannel()

            // 1. Check Low Stock if enabled
            if (settings?.lowStockAlert == true) {
                val lowStockProducts = database.productDao().getAllProductsDirect()
                    .filter { it.stockQuantity <= it.minimumStock && it.active }

                if (lowStockProducts.isNotEmpty()) {
                    showNotification(
                        id = NOTIFICATION_ID_STOCK,
                        title = "Low Stock Alert (${lowStockProducts.size} items)",
                        message = "${lowStockProducts.first().name} and others are running low on stock."
                    )
                }
            }

            // 2. Check Overdue Invoices
            val dueInvoices = database.invoiceDao().getAllInvoicesDirect()
                .filter { it.dueAmount > 0 && it.status != "CANCELLED" }

            if (dueInvoices.isNotEmpty()) {
                val totalDue = dueInvoices.sumOf { it.dueAmount }
                showNotification(
                    id = NOTIFICATION_ID_DUE,
                    title = "Pending Dues Reminder",
                    message = "${dueInvoices.size} invoices have pending balance total ₹$totalDue."
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic business alerts for low stock and pending receivables"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(id: Int, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }
}
