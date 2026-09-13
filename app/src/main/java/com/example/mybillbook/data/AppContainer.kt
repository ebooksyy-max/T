package com.example.mybillbook.data

import android.content.Context
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.repository.*

class AppContainer(val context: Context) {
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val customerRepository: CustomerRepository by lazy {
        CustomerRepository(database)
    }

    val supplierRepository: SupplierRepository by lazy {
        SupplierRepository(database)
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(database)
    }

    val invoiceRepository: InvoiceRepository by lazy {
        InvoiceRepository(database)
    }

    val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepository(database)
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(database)
    }

    val khataRepository: KhataRepository by lazy {
        KhataRepository(database)
    }

    val stockRepository: StockRepository by lazy {
        StockRepository(database)
    }

    val reportRepository: ReportRepository by lazy {
        ReportRepository(database)
    }

    val businessRepository: BusinessRepository by lazy {
        BusinessRepository(database)
    }
}
