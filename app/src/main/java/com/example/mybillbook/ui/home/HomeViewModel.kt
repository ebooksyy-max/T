package com.example.mybillbook.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.SampleDataLoader
import com.example.mybillbook.data.entity.BusinessEntity
import com.example.mybillbook.data.entity.InvoiceEntity
import com.example.mybillbook.data.entity.ProductEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val business: BusinessEntity = BusinessEntity(),
    val todaySales: Double = 0.0,
    val todayCollection: Double = 0.0,
    val totalReceivable: Double = 0.0,
    val totalPayable: Double = 0.0,
    val lowStockCount: Int = 0,
    val lowStockProducts: List<ProductEntity> = emptyList(),
    val recentInvoices: List<InvoiceEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isFirstLaunch: Boolean = false
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val endOfDay = System.currentTimeMillis()

        viewModelScope.launch {
            combine(
                container.businessRepository.business,
                container.invoiceRepository.getTodaySales(startOfDay, endOfDay),
                container.invoiceRepository.getTodayCollection(startOfDay, endOfDay),
                container.invoiceRepository.totalReceivable,
                container.purchaseRepository.totalPayable,
                container.productRepository.lowStockProducts,
                container.invoiceRepository.getRecentInvoices(6)
            ) { args: Array<Any?> ->
                @Suppress("UNCHECKED_CAST")
                val business = args[0] as? BusinessEntity ?: BusinessEntity()
                val todaySales = args[1] as? Double ?: 0.0
                val todayCollection = args[2] as? Double ?: 0.0
                val receivable = args[3] as? Double ?: 0.0
                val payable = args[4] as? Double ?: 0.0
                @Suppress("UNCHECKED_CAST")
                val lowStock = args[5] as? List<ProductEntity> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val recentInvoices = args[6] as? List<InvoiceEntity> ?: emptyList()

                HomeUiState(
                    business = business,
                    todaySales = todaySales,
                    todayCollection = todayCollection,
                    totalReceivable = receivable,
                    totalPayable = payable,
                    lowStockCount = lowStock.size,
                    lowStockProducts = lowStock,
                    recentInvoices = recentInvoices,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            SampleDataLoader.loadRealisticSampleData(container)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
