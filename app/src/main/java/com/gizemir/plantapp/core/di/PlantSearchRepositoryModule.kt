package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.repository.plant_search.PlantSearchRepositoryImpl
import com.gizemir.plantapp.domain.repository.plant_search.PlantSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlantSearchRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlantSearchRepository(
        plantSearchRepositoryImpl: PlantSearchRepositoryImpl
    ): PlantSearchRepository
}
