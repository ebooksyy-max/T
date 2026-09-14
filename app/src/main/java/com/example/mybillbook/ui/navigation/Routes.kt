package com.example.mybillbook.ui.navigation

object Routes {
    const val HOME = "home"
    const val BILLS = "bills"
    const val CREATE_BILL = "create_bill?draftId={draftId}"
    fun createBill(draftId: Long? = null) = if (draftId != null) "create_bill?draftId=$draftId" else "create_bill"

    const val BILL_DETAILS = "bill_details/{invoiceId}"
    fun billDetails(invoiceId: Long) = "bill_details/$invoiceId"

    const val BILL_PREVIEW = "bill_preview/{invoiceId}"
    fun billPreview(invoiceId: Long) = "bill_preview/$invoiceId"

    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAILS = "customer_details/{customerId}"
    fun customerDetails(customerId: Long) = "customer_details/$customerId"

    const val ADD_EDIT_CUSTOMER = "add_edit_customer?customerId={customerId}"
    fun addEditCustomer(customerId: Long? = null) = if (customerId != null) "add_edit_customer?customerId=$customerId" else "add_edit_customer"

    const val KHATA = "khata"
    const val KHATA_DETAILS = "khata_details/{customerId}"
    fun khataDetails(customerId: Long) = "khata_details/$customerId"

    const val RECORD_PAYMENT = "record_payment?partyType={partyType}&partyId={partyId}&invoiceId={invoiceId}"
    fun recordPayment(partyType: String = "CUSTOMER", partyId: Long? = null, invoiceId: Long? = null): String {
        val pId = partyId ?: 0L
        val invId = invoiceId ?: 0L
        return "record_payment?partyType=$partyType&partyId=$pId&invoiceId=$invId"
    }

    const val PRODUCTS = "products"
    const val PRODUCT_DETAILS = "product_details/{productId}"
    fun productDetails(productId: Long) = "product_details/$productId"

    const val ADD_EDIT_PRODUCT = "add_edit_product?productId={productId}"
    fun addEditProduct(productId: Long? = null) = if (productId != null) "add_edit_product?productId=$productId" else "add_edit_product"

    const val INVENTORY = "inventory"
    const val STOCK_ADJUSTMENT = "stock_adjustment/{productId}"
    fun stockAdjustment(productId: Long) = "stock_adjustment/$productId"

    const val PURCHASES = "purchases"
    const val CREATE_PURCHASE = "create_purchase"
    const val PURCHASE_DETAILS = "purchase_details/{purchaseId}"
    fun purchaseDetails(purchaseId: Long) = "purchase_details/$purchaseId"

    const val SUPPLIERS = "suppliers"
    const val ADD_EDIT_SUPPLIER = "add_edit_supplier?supplierId={supplierId}"
    fun addEditSupplier(supplierId: Long? = null) = if (supplierId != null) "add_edit_supplier?supplierId=$supplierId" else "add_edit_supplier"

    const val REPORTS = "reports"
    const val SALES_REPORT = "sales_report"
    const val PURCHASE_REPORT = "purchase_report"
    const val PROFIT_REPORT = "profit_report"
    const val STOCK_REPORT = "stock_report"
    const val DUE_REPORT = "due_report"

    const val SETTINGS = "settings"
    const val BUSINESS_PROFILE = "business_profile"
    const val INVOICE_SETTINGS = "invoice_settings"
    const val TAX_SETTINGS = "tax_settings"
    const val BACKUP_RESTORE = "backup_restore"
    const val APP_SETTINGS = "app_settings"
    const val AD_SETTINGS = "ad_settings"
    const val ONBOARDING = "onboarding"
}
