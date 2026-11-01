package com.kuota.core.data.di

import com.kuota.core.data.repository.ConfigRepository
import com.kuota.core.data.repository.FirebaseConfigRepository
import com.kuota.core.data.repository.FirebaseUserRepository
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
    abstract fun bindConfigRepository(
        impl: FirebaseConfigRepository
    ): ConfigRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: FirebaseUserRepository
    ): UserRepository
}
