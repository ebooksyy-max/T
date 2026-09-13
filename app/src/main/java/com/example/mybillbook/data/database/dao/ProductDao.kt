package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)

    @Query("SELECT * FROM products WHERE active = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE active = 1 ORDER BY name ASC")
    suspend fun getAllProductsDirect(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductByIdDirect(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE active = 1 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE active = 1 AND categoryId = :catId ORDER BY name ASC")
    fun getProductsByCategory(catId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE active = 1 AND stockQuantity <= minimumStock ORDER BY stockQuantity ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE active = 1 AND stockQuantity <= minimumStock")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE active = 1")
    fun getTotalProductCount(): Flow<Int>

    @Query("UPDATE products SET stockQuantity = :newStock, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateStock(productId: Long, newStock: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stockQuantity = stockQuantity + :delta, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun incrementStock(productId: Long, delta: Double, updatedAt: Long = System.currentTimeMillis())
}
