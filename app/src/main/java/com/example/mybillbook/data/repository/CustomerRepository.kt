package com.example.mybillbook.data.repository

import androidx.room.withTransaction
import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.CustomerEntity
import com.example.mybillbook.data.entity.KhataTransactionEntity
import com.example.mybillbook.data.entity.KhataTransactionType
import com.example.mybillbook.domain.model.CustomerBalanceSummary
import kotlinx.coroutines.flow.Flow

class CustomerRepository(private val database: AppDatabase) {
    private val customerDao = database.customerDao()
    private val khataDao = database.khataTransactionDao()

    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)

    fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)

    suspend fun getCustomerByIdDirect(id: Long): CustomerEntity? = customerDao.getCustomerByIdDirect(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long = database.withTransaction {
        val id = customerDao.insertCustomer(customer)
        if (customer.openingBalance > 0.0) {
            khataDao.insertTransaction(
                KhataTransactionEntity(
                    customerId = id,
                    transactionType = KhataTransactionType.CREDIT.name,
                    referenceType = "OPENING_BALANCE",
                    amount = customer.openingBalance,
                    note = "Opening Balance Due"
                )
            )
        }
        id
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDao.updateCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteCustomer(id: Long) {
        customerDao.deleteCustomerById(id)
    }

    suspend fun getCustomerBalanceSummary(customerId: Long): CustomerBalanceSummary {
        val credits = khataDao.getCustomerTotalCredit(customerId)
        val debits = khataDao.getCustomerTotalDebit(customerId)
        val payments = khataDao.getCustomerTotalPayment(customerId)
        // Net due from customer = Credits - Debits - Payments
        val netBalance = credits - debits - payments
        return CustomerBalanceSummary(
            customerId = customerId,
            totalCredit = credits,
            totalDebit = debits,
            totalPayment = payments,
            netBalance = netBalance
        )
    }

    fun getTransactionsForCustomer(customerId: Long): Flow<List<KhataTransactionEntity>> {
        return khataDao.getTransactionsForCustomer(customerId)
    }
}
