package com.example.mybillbook.utils

import android.content.Context
import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    suspend fun exportBackup(context: Context, database: AppDatabase): File = createFullBackup(context, database)

    suspend fun createFullBackup(context: Context, database: AppDatabase): File {
        val root = JSONObject()
        root.put("version", 1)
        root.put("backupDate", System.currentTimeMillis())
        root.put("appName", "MyBillBook")

        // 1. Business
        val business = database.businessDao().getBusinessDirect()
        if (business != null) {
            val bObj = JSONObject().apply {
                put("businessName", business.businessName)
                put("ownerName", business.ownerName)
                put("phone", business.phone)
                put("email", business.email)
                put("address", business.address)
                put("city", business.city)
                put("state", business.state)
                put("pincode", business.pincode)
                put("gstin", business.gstin)
                put("pan", business.pan)
                put("upiId", business.upiId)
                put("bankName", business.bankName)
                put("accountNumber", business.accountNumber)
                put("ifsc", business.ifsc)
            }
            root.put("business", bObj)
        }

        // 2. Customers
        val customers = database.customerDao().getAllCustomersDirect()
        val custArray = JSONArray()
        for (c in customers) {
            val cObj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("email", c.email)
                put("address", c.address)
                put("city", c.city)
                put("state", c.state)
                put("pincode", c.pincode)
                put("gstin", c.gstin)
                put("openingBalance", c.openingBalance)
                put("notes", c.notes)
            }
            custArray.put(cObj)
        }
        root.put("customers", custArray)

        // 3. Suppliers
        val suppliers = database.supplierDao().getAllSuppliersDirect()
        val suppArray = JSONArray()
        for (s in suppliers) {
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("companyName", s.companyName)
                put("phone", s.phone)
                put("email", s.email)
                put("address", s.address)
                put("city", s.city)
                put("state", s.state)
                put("gstin", s.gstin)
                put("openingBalance", s.openingBalance)
            }
            suppArray.put(sObj)
        }
        root.put("suppliers", suppArray)

        // 4. Products
        val products = database.productDao().getAllProductsDirect()
        val prodArray = JSONArray()
        for (p in products) {
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("sku", p.sku)
                put("barcode", p.barcode)
                put("categoryId", p.categoryId ?: 0L)
                put("unit", p.unit)
                put("purchasePrice", p.purchasePrice)
                put("sellingPrice", p.sellingPrice)
                put("gstRate", p.gstRate)
                put("stockQuantity", p.stockQuantity)
                put("minimumStock", p.minimumStock)
                put("description", p.description)
                put("active", p.active)
            }
            prodArray.put(pObj)
        }
        root.put("products", prodArray)

        // 5. Invoices & Items
        val invoices = database.invoiceDao().getAllInvoicesDirect()
        val invArray = JSONArray()
        for (inv in invoices) {
            val items = database.invoiceItemDao().getItemsForInvoiceDirect(inv.id)
            val itemsArr = JSONArray()
            for (it in items) {
                val itObj = JSONObject().apply {
                    put("productId", it.productId ?: 0L)
                    put("productName", it.productNameSnapshot)
                    put("quantity", it.quantity)
                    put("unit", it.unit)
                    put("rate", it.rate)
                    put("discount", it.discount)
                    put("taxRate", it.taxRate)
                    put("taxAmount", it.taxAmount)
                    put("amount", it.amount)
                }
                itemsArr.put(itObj)
            }
            val invObj = JSONObject().apply {
                put("id", inv.id)
                put("invoiceNumber", inv.invoiceNumber)
                put("customerId", inv.customerId ?: 0L)
                put("customerName", inv.customerNameSnapshot)
                put("customerPhone", inv.customerPhoneSnapshot)
                put("invoiceDate", inv.invoiceDate)
                put("subtotal", inv.subtotal)
                put("discount", inv.discount)
                put("taxAmount", inv.taxAmount)
                put("grandTotal", inv.grandTotal)
                put("receivedAmount", inv.receivedAmount)
                put("dueAmount", inv.dueAmount)
                put("paymentMethod", inv.paymentMethod)
                put("status", inv.status)
                put("notes", inv.notes)
                put("items", itemsArr)
            }
            invArray.put(invObj)
        }
        root.put("invoices", invArray)

        val jsonString = root.toString(2)

        // Save into zip file
        val backupDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "backups").apply { mkdirs() }
        val zipFile = File(backupDir, "MyBillBook_Backup_${DateUtils.formatForFileName()}.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            val entry = ZipEntry("backup_data.json")
            zos.putNextEntry(entry)
            zos.write(jsonString.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }

        return zipFile
    }

    suspend fun restoreFromBackup(context: Context, zipFile: File, database: AppDatabase): Boolean {
        return try {
            var jsonString = ""
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    if (entry.name == "backup_data.json") {
                        jsonString = zis.bufferedReader(Charsets.UTF_8).readText()
                        break
                    }
                    entry = zis.nextEntry
                }
            }

            if (jsonString.isBlank()) return false

            val root = JSONObject(jsonString)

            database.withTransaction {
                // Restore Business
                if (root.has("business")) {
                    val bObj = root.getJSONObject("business")
                    database.businessDao().insertOrUpdate(
                        BusinessEntity(
                            id = 1L,
                            businessName = bObj.optString("businessName", "My Business"),
                            ownerName = bObj.optString("ownerName", ""),
                            phone = bObj.optString("phone", ""),
                            email = bObj.optString("email", ""),
                            address = bObj.optString("address", ""),
                            city = bObj.optString("city", ""),
                            state = bObj.optString("state", ""),
                            pincode = bObj.optString("pincode", ""),
                            gstin = bObj.optString("gstin", ""),
                            pan = bObj.optString("pan", ""),
                            upiId = bObj.optString("upiId", ""),
                            bankName = bObj.optString("bankName", ""),
                            accountNumber = bObj.optString("accountNumber", ""),
                            ifsc = bObj.optString("ifsc", "")
                        )
                    )
                }

                // Restore Customers
                if (root.has("customers")) {
                    val custArr = root.getJSONArray("customers")
                    for (i in 0 until custArr.length()) {
                        val cObj = custArr.getJSONObject(i)
                        database.customerDao().insertCustomer(
                            CustomerEntity(
                                id = cObj.optLong("id", 0L),
                                name = cObj.getString("name"),
                                phone = cObj.optString("phone", ""),
                                email = cObj.optString("email", ""),
                                address = cObj.optString("address", ""),
                                city = cObj.optString("city", ""),
                                state = cObj.optString("state", ""),
                                pincode = cObj.optString("pincode", ""),
                                gstin = cObj.optString("gstin", ""),
                                openingBalance = cObj.optDouble("openingBalance", 0.0),
                                notes = cObj.optString("notes", "")
                            )
                        )
                    }
                }

                // Restore Products
                if (root.has("products")) {
                    val prodArr = root.getJSONArray("products")
                    for (i in 0 until prodArr.length()) {
                        val pObj = prodArr.getJSONObject(i)
                        database.productDao().insertProduct(
                            ProductEntity(
                                id = pObj.optLong("id", 0L),
                                name = pObj.getString("name"),
                                sku = pObj.optString("sku", ""),
                                barcode = pObj.optString("barcode", ""),
                                unit = pObj.optString("unit", "Pcs"),
                                purchasePrice = pObj.optDouble("purchasePrice", 0.0),
                                sellingPrice = pObj.optDouble("sellingPrice", 0.0),
                                gstRate = pObj.optDouble("gstRate", 0.0),
                                stockQuantity = pObj.optDouble("stockQuantity", 0.0),
                                minimumStock = pObj.optDouble("minimumStock", 5.0),
                                description = pObj.optString("description", ""),
                                active = pObj.optBoolean("active", true)
                            )
                        )
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
