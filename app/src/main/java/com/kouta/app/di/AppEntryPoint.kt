package com.kouta.app.di

import com.kuota.core.data.repository.AuthRepository
import com.kuota.core.data.repository.ConfigRepository
import com.kuota.core.data.repository.KycRepository
import com.kuota.core.data.repository.PurchaseRepository
import com.kuota.core.data.repository.SalesRepository
import com.kuota.core.data.repository.SupplierRepository
import com.kuota.core.data.repository.UserRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {
    fun authRepository(): AuthRepository
    fun userRepository(): UserRepository
    fun configRepository(): ConfigRepository
    fun kycRepository(): KycRepository
    fun purchaseRepository(): PurchaseRepository
    fun supplierRepository(): SupplierRepository
    fun salesRepository(): SalesRepository
}
