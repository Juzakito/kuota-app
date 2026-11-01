package com.kuota.core.data.di

import com.kuota.core.data.repository.AuthRepository
import com.kuota.core.data.repository.ConfigRepository
import com.kuota.core.data.repository.DefaultKycRepository
import com.kuota.core.data.repository.FirebaseAuthRepository
import com.kuota.core.data.repository.FirebaseConfigRepository
import com.kuota.core.data.repository.FirebasePurchaseRepository
import com.kuota.core.data.repository.FirebaseSupplierRepository
import com.kuota.core.data.repository.FirebaseUserRepository
import com.kuota.core.data.repository.FirebaseSalesRepository
import com.kuota.core.data.repository.KycRepository
import com.kuota.core.data.repository.PurchaseRepository
import com.kuota.core.data.repository.SalesRepository
import com.kuota.core.data.repository.SupplierRepository
import com.kuota.core.data.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        impl: FirebaseConfigRepository
    ): ConfigRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: FirebaseUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindKycRepository(
        impl: DefaultKycRepository
    ): KycRepository

    @Binds
    @Singleton
    abstract fun bindPurchaseRepository(
        impl: FirebasePurchaseRepository
    ): PurchaseRepository

    @Binds
    @Singleton
    abstract fun bindSupplierRepository(
        impl: FirebaseSupplierRepository
    ): SupplierRepository

    @Binds
    @Singleton
    abstract fun bindSalesRepository(
        impl: FirebaseSalesRepository
    ): SalesRepository
}
