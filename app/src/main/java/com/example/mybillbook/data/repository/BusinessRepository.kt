package com.example.mybillbook.data.repository

import com.example.mybillbook.data.database.AppDatabase
import com.example.mybillbook.data.entity.*
import kotlinx.coroutines.flow.Flow

class BusinessRepository(private val database: AppDatabase) {
    private val businessDao = database.businessDao()
    private val settingsDao = database.settingsDao()

    val business: Flow<BusinessEntity?> = businessDao.getBusiness()
    val appSettings: Flow<AppSettingsEntity?> = settingsDao.getAppSettings()
    val invoiceSettings: Flow<InvoiceSettingsEntity?> = settingsDao.getInvoiceSettings()

    suspend fun getBusinessDirect(): BusinessEntity =
        businessDao.getBusinessDirect() ?: BusinessEntity()

    suspend fun updateBusiness(business: BusinessEntity) {
        businessDao.insertOrUpdate(business.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun getAppSettingsDirect(): AppSettingsEntity =
        settingsDao.getAppSettingsDirect() ?: AppSettingsEntity()

    suspend fun updateAppSettings(settings: AppSettingsEntity) {
        settingsDao.insertOrUpdateAppSettings(settings)
    }

    suspend fun getInvoiceSettingsDirect(): InvoiceSettingsEntity =
        settingsDao.getInvoiceSettingsDirect() ?: InvoiceSettingsEntity()

    suspend fun updateInvoiceSettings(settings: InvoiceSettingsEntity) {
        settingsDao.insertOrUpdateInvoiceSettings(settings)
    }

    suspend fun completeFirstLaunch() {
        val current = getAppSettingsDirect()
        settingsDao.insertOrUpdateAppSettings(current.copy(firstLaunchCompleted = true))
    }
}
