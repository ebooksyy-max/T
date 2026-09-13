package com.example.mybillbook.data.repository

import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.CategoryEntity
import com.example.mybillbook.data.entity.ProductEntity
import com.example.mybillbook.data.entity.StockTransactionEntity
import com.example.mybillbook.data.entity.StockTransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()
    private val stockTransactionDao = database.stockTransactionDao()

    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()
    val lowStockCount: Flow<Int> = productDao.getLowStockCount()
    val totalProductCount: Flow<Int> = productDao.getTotalProductCount()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val totalStockValue: Flow<Double> = productDao.getAllProducts().map { list ->
        list.sumOf { it.stockQuantity * it.purchasePrice }
    }

    suspend fun getAllCategoriesDirect(): List<CategoryEntity> = categoryDao.getAllCategoriesDirect()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)

    suspend fun getProductByIdDirect(id: Long): ProductEntity? = productDao.getProductByIdDirect(id)

    suspend fun insertProduct(product: ProductEntity): Long = database.withTransaction {
        val id = productDao.insertProduct(product)
        if (product.stockQuantity > 0.0) {
            stockTransactionDao.insertTransaction(
                StockTransactionEntity(
                    productId = id,
                    transactionType = StockTransactionType.ADJUSTMENT_IN.name,
                    quantity = product.stockQuantity,
                    note = "Opening Stock"
                )
            )
        }
        id
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(id: Long) {
        productDao.deleteProductById(id)
    }

    suspend fun insertCategory(categoryName: String): Long {
        return categoryDao.insertCategory(CategoryEntity(name = categoryName.trim()))
    }

    suspend fun adjustStock(
        productId: Long,
        adjustmentQuantity: Double,
        isAdd: Boolean,
        reason: String
    ) = database.withTransaction {
        val product = productDao.getProductByIdDirect(productId) ?: return@withTransaction
        val delta = if (isAdd) adjustmentQuantity else -adjustmentQuantity
        val newStock = (product.stockQuantity + delta).coerceAtLeast(0.0)
        productDao.updateStock(productId, newStock)

        stockTransactionDao.insertTransaction(
            StockTransactionEntity(
                productId = productId,
                transactionType = if (isAdd) StockTransactionType.ADJUSTMENT_IN.name else StockTransactionType.ADJUSTMENT_OUT.name,
                quantity = adjustmentQuantity,
                note = reason.ifBlank { if (isAdd) "Stock Added" else "Stock Deducted" }
            )
        )
    }

    fun getStockHistory(productId: Long): Flow<List<StockTransactionEntity>> {
        return stockTransactionDao.getTransactionsForProduct(productId)
    }
}
