package com.example.mybillbook.data.database.dao

import androidx.room.*
import com.example.mybillbook.data.entity.AppSettingsEntity
import com.example.mybillbook.data.entity.ExpenseEntity
import com.example.mybillbook.data.entity.InvoiceSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getAppSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getAppSettingsDirect(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppSettings(settings: AppSettingsEntity)

    @Query("SELECT * FROM invoice_settings WHERE id = 1 LIMIT 1")
    fun getInvoiceSettings(): Flow<InvoiceSettingsEntity?>

    @Query("SELECT * FROM invoice_settings WHERE id = 1 LIMIT 1")
    suspend fun getInvoiceSettingsDirect(): InvoiceSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInvoiceSettings(settings: InvoiceSettingsEntity)

    @Query("UPDATE invoice_settings SET nextInvoiceNumber = nextInvoiceNumber + 1 WHERE id = 1")
    suspend fun incrementInvoiceNumber()

    @Query("UPDATE invoice_settings SET nextPurchaseNumber = nextPurchaseNumber + 1 WHERE id = 1")
    suspend fun incrementPurchaseNumber()
}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseDate >= :start AND expenseDate <= :end ORDER BY expenseDate DESC")
    fun getExpensesBetween(start: Long, end: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE expenseDate >= :start AND expenseDate <= :end ORDER BY expenseDate DESC")
    suspend fun getExpensesBetweenDirect(start: Long, end: Long): List<ExpenseEntity>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE expenseDate >= :start AND expenseDate <= :end")
    fun getTotalExpensesBetween(start: Long, end: Long): Flow<Double>
}
