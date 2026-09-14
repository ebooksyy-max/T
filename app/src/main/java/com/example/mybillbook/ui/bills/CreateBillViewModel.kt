package com.example.mybillbook.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.*
import com.example.mybillbook.domain.model.BillCalculation
import com.example.mybillbook.domain.model.BillItemInput
import com.example.mybillbook.domain.usecase.BillingCalculator
import com.example.mybillbook.utils.DateUtils
import com.example.mybillbook.utils.InvoiceNumberGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CreateBillUiState(
    val invoiceNumber: String = "",
    val invoiceDate: Long = System.currentTimeMillis(),
    val selectedCustomer: CustomerEntity? = null,
    val customerNameInput: String = "",
    val customerPhoneInput: String = "",
    val allCustomers: List<CustomerEntity> = emptyList(),
    val allProducts: List<ProductEntity> = emptyList(),
    val items: List<BillItemInput> = emptyList(),
    val billDiscount: Double = 0.0,
    val receivedAmount: Double = 0.0,
    val paymentMethod: String = PaymentMethods.CASH,
    val notes: String = "",
    val calculation: BillCalculation = BillingCalculator.calculateBill(emptyList()),
    val business: BusinessEntity = BusinessEntity(),
    val invoiceSettings: InvoiceSettingsEntity = InvoiceSettingsEntity(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedInvoiceId: Long? = null
)

class CreateBillViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBillUiState())
    val uiState: StateFlow<CreateBillUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val business = container.businessRepository.getBusinessDirect()
            val settings = container.businessRepository.getInvoiceSettingsDirect()
            val invNumber = InvoiceNumberGenerator.generateInvoiceNumber(
                settings.invoicePrefix,
                settings.nextInvoiceNumber
            )

            container.customerRepository.allCustomers.collect { customers ->
                _uiState.update { current ->
                    current.copy(
                        business = business,
                        invoiceSettings = settings,
                        invoiceNumber = invNumber,
                        allCustomers = customers
                    )
                }
            }
        }

        viewModelScope.launch {
            container.productRepository.allProducts.collect { products ->
                _uiState.update { it.copy(allProducts = products) }
            }
        }
    }

    fun selectCustomer(customer: CustomerEntity?) {
        _uiState.update { current ->
            val updated = current.copy(
                selectedCustomer = customer,
                customerNameInput = customer?.name ?: "",
                customerPhoneInput = customer?.phone ?: ""
            )
            recalculate(updated)
        }
    }

    fun setCustomerManual(name: String, phone: String) {
        _uiState.update { current ->
            current.copy(
                selectedCustomer = null,
                customerNameInput = name,
                customerPhoneInput = phone
            )
        }
    }

    fun addProductItem(product: ProductEntity) {
        val existingIndex = _uiState.value.items.indexOfFirst { it.productId == product.id }
        if (existingIndex >= 0) {
            // Increment existing item
            updateItemQuantity(existingIndex, _uiState.value.items[existingIndex].quantity + 1.0)
        } else {
            val newItem = BillItemInput(
                productId = product.id,
                productName = product.name,
                quantity = 1.0,
                unit = product.unit,
                rate = product.sellingPrice,
                discount = 0.0,
                taxRate = product.gstRate,
                isTaxInclusive = _uiState.value.invoiceSettings.taxInclusive,
                currentStock = product.stockQuantity
            )
            _uiState.update { current ->
                val newItems = current.items + newItem
                val updated = current.copy(items = newItems)
                recalculate(updated)
            }
        }
    }

    fun addCustomItem(name: String, quantity: Double, rate: Double, unit: String, taxRate: Double) {
        val newItem = BillItemInput(
            productId = null,
            productName = name.ifBlank { "Custom Item" },
            quantity = quantity.coerceAtLeast(0.01),
            unit = unit.ifBlank { "Pcs" },
            rate = rate.coerceAtLeast(0.0),
            discount = 0.0,
            taxRate = taxRate.coerceAtLeast(0.0),
            isTaxInclusive = _uiState.value.invoiceSettings.taxInclusive
        )
        _uiState.update { current ->
            val newItems = current.items + newItem
            val updated = current.copy(items = newItems)
            recalculate(updated)
        }
    }

    fun updateItemQuantity(index: Int, newQty: Double) {
        if (index !in _uiState.value.items.indices) return
        val currentItems = _uiState.value.items.toMutableList()
        if (newQty <= 0) {
            currentItems.removeAt(index)
        } else {
            currentItems[index] = currentItems[index].copy(quantity = newQty)
        }
        _uiState.update { current ->
            val updated = current.copy(items = currentItems)
            recalculate(updated)
        }
    }

    fun updateItemRate(index: Int, newRate: Double) {
        if (index !in _uiState.value.items.indices) return
        val currentItems = _uiState.value.items.toMutableList()
        currentItems[index] = currentItems[index].copy(rate = newRate.coerceAtLeast(0.0))
        _uiState.update { current ->
            val updated = current.copy(items = currentItems)
            recalculate(updated)
        }
    }

    fun updateItemDiscount(index: Int, newDiscount: Double) {
        if (index !in _uiState.value.items.indices) return
        val currentItems = _uiState.value.items.toMutableList()
        currentItems[index] = currentItems[index].copy(discount = newDiscount.coerceAtLeast(0.0))
        _uiState.update { current ->
            val updated = current.copy(items = currentItems)
            recalculate(updated)
        }
    }

    fun removeItem(index: Int) {
        if (index !in _uiState.value.items.indices) return
        val currentItems = _uiState.value.items.toMutableList()
        currentItems.removeAt(index)
        _uiState.update { current ->
            val updated = current.copy(items = currentItems)
            recalculate(updated)
        }
    }

    fun setBillDiscount(discount: Double) {
        _uiState.update { current ->
            val updated = current.copy(billDiscount = discount.coerceAtLeast(0.0))
            recalculate(updated)
        }
    }

    fun setReceivedAmount(received: Double) {
        _uiState.update { current ->
            val updated = current.copy(receivedAmount = received.coerceAtLeast(0.0))
            recalculate(updated)
        }
    }

    fun setPaymentMethod(method: String) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun setFullPayment() {
        val grandTotal = _uiState.value.calculation.grandTotal
        setReceivedAmount(grandTotal)
    }

    fun setZeroPayment() {
        setReceivedAmount(0.0)
    }

    private fun recalculate(state: CreateBillUiState): CreateBillUiState {
        val calc = BillingCalculator.calculateBill(
            items = state.items,
            additionalDiscount = state.billDiscount,
            receivedAmount = state.receivedAmount,
            businessState = state.business.state,
            customerState = state.selectedCustomer?.state ?: ""
        )
        return state.copy(calculation = calc)
    }

    fun saveInvoice(isDraft: Boolean = false, onSuccess: (Long) -> Unit) {
        val state = _uiState.value
        if (state.items.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please add at least one item to the bill") }
            return
        }

        if (state.customerPhoneInput.isNotBlank() && state.customerPhoneInput.length != 10) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid 10-digit mobile number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val calc = state.calculation

                val invoice = InvoiceEntity(
                    invoiceNumber = state.invoiceNumber,
                    customerId = state.selectedCustomer?.id,
                    customerNameSnapshot = state.customerNameInput.ifBlank { "Walk-in Customer" },
                    customerPhoneSnapshot = state.customerPhoneInput,
                    invoiceDate = state.invoiceDate,
                    subtotal = calc.subtotal,
                    discount = calc.totalDiscount,
                    taxAmount = calc.taxAmount,
                    grandTotal = calc.grandTotal,
                    receivedAmount = calc.receivedAmount,
                    dueAmount = calc.dueAmount,
                    paymentMethod = state.paymentMethod,
                    status = if (isDraft) InvoiceStatus.DRAFT.name else calc.status.name,
                    notes = state.notes
                )

                val invoiceItems = calc.items.map { item ->
                    InvoiceItemEntity(
                        invoiceId = 0L,
                        productId = item.productId,
                        productNameSnapshot = item.productName,
                        quantity = item.quantity,
                        unit = item.unit,
                        rate = item.rate,
                        discount = item.discount,
                        taxRate = item.taxRate,
                        taxAmount = item.taxAmount,
                        amount = item.totalAmount
                    )
                }

                val savedId = if (isDraft) {
                    container.invoiceRepository.saveDraftInvoice(invoice, invoiceItems)
                } else {
                    container.invoiceRepository.saveFinalizedInvoice(invoice, invoiceItems)
                }

                _uiState.update { it.copy(isSaving = false, savedInvoiceId = savedId) }
                onSuccess(savedId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = "Failed to save: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
