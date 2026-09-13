package com.example.mybillbook.ui.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.data.entity.InvoiceStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BillsUiState(
    val invoices: List<InvoiceEntity> = emptyList(),
    val filteredInvoices: List<InvoiceEntity> = emptyList(),
    val selectedFilter: String = "ALL", // ALL, UNPAID, PARTIALLY_PAID, PAID, DRAFT
    val searchQuery: String = "",
    val totalSales: Double = 0.0,
    val totalReceivable: Double = 0.0,
    val isLoading: Boolean = false
)

class BillsViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(BillsUiState())
    val uiState: StateFlow<BillsUiState> = _uiState.asStateFlow()

    init {
        loadInvoices()
    }

    private fun loadInvoices() {
        viewModelScope.launch {
            combine(
                container.invoiceRepository.allInvoices,
                container.invoiceRepository.totalSales,
                container.invoiceRepository.totalReceivable
            ) { invoices, totalSales, totalReceivable ->
                val current = _uiState.value
                val filtered = applyFilterAndSearch(invoices, current.selectedFilter, current.searchQuery)
                current.copy(
                    invoices = invoices,
                    filteredInvoices = filtered,
                    totalSales = totalSales,
                    totalReceivable = totalReceivable,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onFilterSelected(filter: String) {
        val current = _uiState.value
        val filtered = applyFilterAndSearch(current.invoices, filter, current.searchQuery)
        _uiState.value = current.copy(selectedFilter = filter, filteredInvoices = filtered)
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value
        val filtered = applyFilterAndSearch(current.invoices, current.selectedFilter, query)
        _uiState.value = current.copy(searchQuery = query, filteredInvoices = filtered)
    }

    private fun applyFilterAndSearch(
        list: List<InvoiceEntity>,
        filter: String,
        query: String
    ): List<InvoiceEntity> {
        return list.filter { inv ->
            val matchesFilter = when (filter) {
                "ALL" -> true
                "UNPAID" -> inv.status == InvoiceStatus.UNPAID.name
                "PARTIALLY_PAID" -> inv.status == InvoiceStatus.PARTIALLY_PAID.name
                "PAID" -> inv.status == InvoiceStatus.PAID.name
                "DRAFT" -> inv.status == InvoiceStatus.DRAFT.name
                else -> true
            }

            val matchesQuery = query.isBlank() ||
                    inv.invoiceNumber.contains(query, ignoreCase = true) ||
                    inv.customerNameSnapshot.contains(query, ignoreCase = true) ||
                    inv.customerPhoneSnapshot.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }

    fun cancelInvoice(invoiceId: Long) {
        viewModelScope.launch {
            container.invoiceRepository.cancelInvoice(invoiceId)
        }
    }
}
