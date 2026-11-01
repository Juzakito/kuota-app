package com.kouta.app.di

import com.kouta.app.core.util.LocationHelper
import com.kouta.app.core.util.LocationTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindLocationTracker(helper: LocationHelper): LocationTracker
}
