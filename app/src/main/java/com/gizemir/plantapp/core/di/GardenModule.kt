package com.gizemir.plantapp.core.di

import android.content.Context
import com.gizemir.plantapp.data.local.dao.garden.GardenPlantDao
import com.gizemir.plantapp.data.local.PlantAppDatabase
import com.gizemir.plantapp.data.repository.garden.GardenRepositoryImpl
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GardenModule {
    @Provides
    @Singleton
    fun provideGardenPlantDao(db: PlantAppDatabase): GardenPlantDao = db.gardenPlantDao()

    @Provides
    @Singleton
    fun provideGardenRepository(dao: GardenPlantDao): GardenRepository = GardenRepositoryImpl(dao)
} 