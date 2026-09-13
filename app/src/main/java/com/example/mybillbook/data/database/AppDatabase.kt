package com.example.mybillbook.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mybillbook.data.database.dao.*
import com.example.mybillbook.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BusinessEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        PaymentEntity::class,
        KhataTransactionEntity::class,
        StockTransactionEntity::class,
        ExpenseEntity::class,
        AppSettingsEntity::class,
        InvoiceSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun purchaseItemDao(): PurchaseItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun khataTransactionDao(): KhataTransactionDao
    abstract fun stockTransactionDao(): StockTransactionDao
    abstract fun businessDao(): BusinessDao
    abstract fun settingsDao(): SettingsDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "MyBillBookDatabase"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaults(database)
                    }
                }
            }
        }

        suspend fun populateDefaults(database: AppDatabase) {
            // Default business entity
            database.businessDao().insertOrUpdate(
                BusinessEntity(
                    id = 1L,
                    businessName = "My Store",
                    ownerName = "Shop Owner",
                    phone = "",
                    email = "",
                    address = "Main Market",
                    city = "",
                    state = "",
                    pincode = "",
                    gstin = "",
                    upiId = "",
                    bankName = "",
                    accountNumber = "",
                    ifsc = ""
                )
            )

            // Default App Settings
            database.settingsDao().insertOrUpdateAppSettings(
                AppSettingsEntity(
                    id = 1L,
                    currency = "INR",
                    currencySymbol = "₹",
                    language = "English",
                    theme = "SYSTEM",
                    lowStockAlert = true,
                    paymentReminderDays = 7,
                    firstLaunchCompleted = false
                )
            )

            // Default Invoice Settings
            database.settingsDao().insertOrUpdateInvoiceSettings(
                InvoiceSettingsEntity(
                    id = 1L,
                    invoicePrefix = "INV-",
                    nextInvoiceNumber = 1L,
                    purchasePrefix = "PUR-",
                    nextPurchaseNumber = 1L,
                    defaultTaxRate = 18.0,
                    taxInclusive = false,
                    defaultPaymentMethod = PaymentMethods.CASH,
                    template = "STANDARD",
                    showLogo = true,
                    showGstin = true,
                    showSignature = true,
                    showUpiQr = true,
                    footerText = "Thank you for shopping with us! Visit again.",
                    termsAndConditions = "1. Goods once sold will not be returned without bill.\n2. Subject to local jurisdiction."
                )
            )

            // Default Categories
            database.categoryDao().insertCategory(CategoryEntity(name = "General"))
            database.categoryDao().insertCategory(CategoryEntity(name = "Electronics"))
            database.categoryDao().insertCategory(CategoryEntity(name = "Grocery"))
            database.categoryDao().insertCategory(CategoryEntity(name = "Hardware"))
        }
    }
}
