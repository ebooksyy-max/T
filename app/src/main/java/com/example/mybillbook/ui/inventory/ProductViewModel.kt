package com.example.mybillbook.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybillbook.data.AppContainer
import com.example.mybillbook.data.entity.CategoryEntity
import com.example.mybillbook.data.entity.ProductEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class InventoryUiState(
    val products: List<ProductEntity> = emptyList(),
    val filteredProducts: List<ProductEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedCategory: Long? = null,
    val selectedFilter: String = "ALL", // ALL, LOW_STOCK
    val searchQuery: String = "",
    val totalStockValue: Double = 0.0,
    val lowStockCount: Int = 0,
    val isLoading: Boolean = false
)

class ProductViewModel(private val container: AppContainer) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                container.productRepository.allProducts,
                container.productRepository.allCategories,
                container.productRepository.totalStockValue,
                container.productRepository.lowStockProducts
            ) { products, categories, stockVal, lowStock ->
                val current = _uiState.value
                val filtered = applyFilterAndSearch(
                    products,
                    current.selectedFilter,
                    current.selectedCategory,
                    current.searchQuery
                )
                current.copy(
                    products = products,
                    filteredProducts = filtered,
                    categories = categories,
                    totalStockValue = stockVal,
                    lowStockCount = lowStock.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value
        val filtered = applyFilterAndSearch(current.products, current.selectedFilter, current.selectedCategory, query)
        _uiState.value = current.copy(searchQuery = query, filteredProducts = filtered)
    }

    fun onFilterSelected(filter: String) {
        val current = _uiState.value
        val filtered = applyFilterAndSearch(current.products, filter, current.selectedCategory, current.searchQuery)
        _uiState.value = current.copy(selectedFilter = filter, filteredProducts = filtered)
    }

    fun onCategorySelected(categoryId: Long?) {
        val current = _uiState.value
        val filtered = applyFilterAndSearch(current.products, current.selectedFilter, categoryId, current.searchQuery)
        _uiState.value = current.copy(selectedCategory = categoryId, filteredProducts = filtered)
    }

    private fun applyFilterAndSearch(
        list: List<ProductEntity>,
        filter: String,
        category: Long?,
        query: String
    ): List<ProductEntity> {
        return list.filter { p ->
            val matchesFilter = when (filter) {
                "LOW_STOCK" -> p.stockQuantity <= p.minimumStock
                else -> true
            }

            val matchesCategory = category == null || p.categoryId == category

            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.sku.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query)

            matchesFilter && matchesCategory && matchesQuery
        }
    }

    fun adjustStock(productId: Long, type: String, quantity: Double, note: String) {
        viewModelScope.launch {
            val product = container.productRepository.getProductByIdDirect(productId) ?: return@launch
            val qtyBefore = product.stockQuantity
            when (type) {
                "ADD" -> {
                    container.productRepository.adjustStock(productId, quantity, isAdd = true, reason = note)
                }
                "REDUCE" -> {
                    container.productRepository.adjustStock(productId, quantity, isAdd = false, reason = note)
                }
                "SET" -> {
                    val delta = quantity - qtyBefore
                    if (delta >= 0) {
                        container.productRepository.adjustStock(productId, delta, isAdd = true, reason = note.ifBlank { "Stock count set to $quantity" })
                    } else {
                        container.productRepository.adjustStock(productId, -delta, isAdd = false, reason = note.ifBlank { "Stock count set to $quantity" })
                    }
                }
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            container.productRepository.deleteProduct(productId)
        }
    }
}
