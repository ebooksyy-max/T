package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Update
    suspend fun updateSupplier(supplier: SupplierEntity)

    @Delete
    suspend fun deleteSupplier(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: Long)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    suspend fun getAllSuppliersDirect(): List<SupplierEntity>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    fun getSupplierById(id: Long): Flow<SupplierEntity?>

    @Query("SELECT * FROM suppliers WHERE id = :id LIMIT 1")
    suspend fun getSupplierByIdDirect(id: Long): SupplierEntity?

    @Query("SELECT * FROM suppliers WHERE name LIKE '%' || :query || '%' OR companyName LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSuppliers(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT COUNT(*) FROM suppliers")
    fun getSupplierCount(): Flow<Int>
}
