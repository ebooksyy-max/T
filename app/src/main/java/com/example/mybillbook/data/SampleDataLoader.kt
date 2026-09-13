package com.example.mybillbook.data

import com.example.mybillbook.data.entity.*
import com.example.mybillbook.domain.usecase.BillingCalculator

object SampleDataLoader {

    suspend fun loadRealisticSampleData(container: AppContainer) {
        val custRepo = container.customerRepository
        val suppRepo = container.supplierRepository
        val prodRepo = container.productRepository
        val invRepo = container.invoiceRepository
        val bRepo = container.businessRepository

        // 1. Update Business Profile
        bRepo.updateBusiness(
            BusinessEntity(
                id = 1L,
                businessName = "Apex Retail Traders",
                ownerName = "Rajesh Sharma",
                phone = "+91 98765 43210",
                email = "apexretail@example.com",
                address = "Shop 14, Commercial Complex, M.G. Road",
                city = "Mumbai",
                state = "Maharashtra",
                pincode = "400001",
                gstin = "27AABCA1234F1Z5",
                pan = "AABCA1234F",
                upiId = "apexretail@okhdfcbank",
                bankName = "HDFC Bank",
                accountNumber = "50200012345678",
                ifsc = "HDFC0001234"
            )
        )

        // 2. Customers
        val c1 = custRepo.insertCustomer(
            CustomerEntity(
                name = "Aarav Patel",
                phone = "9820011223",
                email = "aarav.patel@example.com",
                address = "Flat 402, Sunshine Heights",
                city = "Mumbai",
                state = "Maharashtra",
                pincode = "400053",
                openingBalance = 1500.0,
                notes = "Regular customer"
            )
        )

        val c2 = custRepo.insertCustomer(
            CustomerEntity(
                name = "Priya Sharma",
                phone = "9819922334",
                email = "priya.sharma@example.com",
                address = "B-12, Greenfield Apts",
                city = "Mumbai",
                state = "Maharashtra",
                pincode = "400058",
                openingBalance = 0.0,
                notes = "Preferred cash/UPI"
            )
        )

        val c3 = custRepo.insertCustomer(
            CustomerEntity(
                name = "Rohit Verma",
                phone = "9769933445",
                address = "Sector 19, Vashi",
                city = "Navi Mumbai",
                state = "Maharashtra",
                openingBalance = 3200.0
            )
        )

        // 3. Suppliers
        val s1 = suppRepo.insertSupplier(
            SupplierEntity(
                name = "Vikas Wholesalers",
                companyName = "Vikas Wholesale Mart Pvt Ltd",
                phone = "9988776655",
                address = "Gala 4, APMC Market",
                city = "Navi Mumbai",
                state = "Maharashtra",
                gstin = "27AAACV9876Q1ZB",
                openingBalance = 5000.0
            )
        )

        // 4. Products
        val catElec = prodRepo.insertCategory("Electronics")
        val catStat = prodRepo.insertCategory("Stationery")
        val catPack = prodRepo.insertCategory("Packaging")

        val p1 = prodRepo.insertProduct(
            ProductEntity(
                name = "Wireless Optical Mouse",
                sku = "TECH-MOU-01",
                barcode = "8901234567890",
                categoryId = catElec,
                unit = "Pcs",
                purchasePrice = 320.0,
                sellingPrice = 499.0,
                gstRate = 18.0,
                stockQuantity = 45.0,
                minimumStock = 10.0,
                description = "2.4GHz ergonomically designed wireless mouse"
            )
        )

        val p2 = prodRepo.insertProduct(
            ProductEntity(
                name = "Mechanical Keyboard RGB",
                sku = "TECH-KB-02",
                barcode = "8901234567891",
                categoryId = catElec,
                unit = "Pcs",
                purchasePrice = 1450.0,
                sellingPrice = 2199.0,
                gstRate = 18.0,
                stockQuantity = 18.0,
                minimumStock = 5.0,
                description = "Blue switch mechanical gaming keyboard"
            )
        )

        val p3 = prodRepo.insertProduct(
            ProductEntity(
                name = "A4 Copier Paper (500 Sheets)",
                sku = "STAT-A4-03",
                barcode = "8901234567892",
                categoryId = catStat,
                unit = "Pack",
                purchasePrice = 210.0,
                sellingPrice = 290.0,
                gstRate = 12.0,
                stockQuantity = 80.0,
                minimumStock = 20.0,
                description = "75 GSM high-brightness photocopy paper"
            )
        )

        val p4 = prodRepo.insertProduct(
            ProductEntity(
                name = "Fast Charging Cable Type-C",
                sku = "TECH-CAB-04",
                barcode = "8901234567893",
                categoryId = catElec,
                unit = "Pcs",
                purchasePrice = 95.0,
                sellingPrice = 199.0,
                gstRate = 18.0,
                stockQuantity = 4.0, // Low stock on purpose for testing!
                minimumStock = 10.0,
                description = "Braided 65W fast charging 1.2m cable"
            )
        )

        // 5. Create Sample Invoices
        // Invoice 1: Paid invoice
        val item1 = InvoiceItemEntity(
            invoiceId = 0L,
            productId = p1,
            productNameSnapshot = "Wireless Optical Mouse",
            quantity = 2.0,
            unit = "Pcs",
            rate = 499.0,
            discount = 0.0,
            taxRate = 18.0,
            taxAmount = 179.64,
            amount = 1177.64
        )
        val invoice1 = InvoiceEntity(
            invoiceNumber = "INV-0001",
            customerId = c2,
            customerNameSnapshot = "Priya Sharma",
            customerPhoneSnapshot = "9819922334",
            invoiceDate = System.currentTimeMillis() - 86400000L * 2,
            subtotal = 998.0,
            discount = 0.0,
            taxAmount = 179.64,
            grandTotal = 1177.64,
            receivedAmount = 1177.64,
            dueAmount = 0.0,
            paymentMethod = PaymentMethods.UPI,
            status = InvoiceStatus.PAID.name,
            notes = "Paid via UPI"
        )
        invRepo.saveFinalizedInvoice(invoice1, listOf(item1))

        // Invoice 2: Partially Paid invoice
        val item2a = InvoiceItemEntity(
            invoiceId = 0L,
            productId = p2,
            productNameSnapshot = "Mechanical Keyboard RGB",
            quantity = 1.0,
            unit = "Pcs",
            rate = 2199.0,
            discount = 100.0,
            taxRate = 18.0,
            taxAmount = 377.82,
            amount = 2476.82
        )
        val item2b = InvoiceItemEntity(
            invoiceId = 0L,
            productId = p3,
            productNameSnapshot = "A4 Copier Paper (500 Sheets)",
            quantity = 2.0,
            unit = "Pack",
            rate = 290.0,
            discount = 0.0,
            taxRate = 12.0,
            taxAmount = 69.60,
            amount = 649.60
        )
        val invoice2 = InvoiceEntity(
            invoiceNumber = "INV-0002",
            customerId = c1,
            customerNameSnapshot = "Aarav Patel",
            customerPhoneSnapshot = "9820011223",
            invoiceDate = System.currentTimeMillis() - 86400000L,
            subtotal = 2679.0,
            discount = 100.0,
            taxAmount = 447.42,
            grandTotal = 3126.42,
            receivedAmount = 2000.0,
            dueAmount = 1126.42,
            paymentMethod = PaymentMethods.CASH,
            status = InvoiceStatus.PARTIALLY_PAID.name,
            notes = "Advance cash received ₹2000"
        )
        invRepo.saveFinalizedInvoice(invoice2, listOf(item2a, item2b))

        bRepo.completeFirstLaunch()
    }
}
