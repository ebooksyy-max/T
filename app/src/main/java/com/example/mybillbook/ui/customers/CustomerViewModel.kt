package com.example.mybillbook.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.CustomerEntity
import com.example.mybillbook.domain.model.CustomerBalanceSummary
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CustomerWithBalance(
    val customer: CustomerEntity,
    val balance: CustomerBalanceSummary
)

data class CustomersUiState(
    val customers: List<CustomerWithBalance> = emptyList(),
    val filteredCustomers: List<CustomerWithBalance> = emptyList(),
    val searchQuery: String = "",
    val totalDue: Double = 0.0,
    val totalAdvance: Double = 0.0,
    val isLoading: Boolean = false
)

class CustomerViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState())
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            container.customerRepository.allCustomers.collect { list ->
                val listWithBalances = list.map { c ->
                    val bal = container.customerRepository.getCustomerBalanceSummary(c.id)
                    CustomerWithBalance(c, bal)
                }
                val totalDue = listWithBalances.filter { it.balance.netBalance > 0 }.sumOf { it.balance.netBalance }
                val totalAdv = listWithBalances.filter { it.balance.netBalance < 0 }.sumOf { -it.balance.netBalance }

                val filtered = applySearch(listWithBalances, _uiState.value.searchQuery)
                _uiState.value = CustomersUiState(
                    customers = listWithBalances,
                    filteredCustomers = filtered,
                    searchQuery = _uiState.value.searchQuery,
                    totalDue = totalDue,
                    totalAdvance = totalAdv,
                    isLoading = false
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val filtered = applySearch(_uiState.value.customers, query)
        _uiState.update { it.copy(searchQuery = query, filteredCustomers = filtered) }
    }

    private fun applySearch(list: List<CustomerWithBalance>, query: String): List<CustomerWithBalance> {
        if (query.isBlank()) return list
        return list.filter {
            it.customer.name.contains(query, ignoreCase = true) ||
                    it.customer.phone.contains(query) ||
                    it.customer.city.contains(query, ignoreCase = true)
        }
    }

    fun saveCustomer(customer: CustomerEntity, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val id = if (customer.id == 0L) {
                container.customerRepository.insertCustomer(customer)
            } else {
                container.customerRepository.updateCustomer(customer)
                customer.id
            }
            loadCustomers()
            onSuccess(id)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch {
            container.customerRepository.deleteCustomer(id)
            loadCustomers()
        }
    }
}
