package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.domain.repository.plant_search.PlantSearchRepository
import com.gizemir.plantapp.domain.use_case.plant_search.GetPlantDetailUseCase
import com.gizemir.plantapp.domain.use_case.plant_search.PlantSearchUseCases
import com.gizemir.plantapp.domain.use_case.plant_search.SearchPlantsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlantSearchUseCaseModule {

    @Provides
    @Singleton
    fun provideGetPlantDetailUseCase(
        repository: PlantSearchRepository
    ): GetPlantDetailUseCase {
        return GetPlantDetailUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchPlantsUseCase(
        repository: PlantSearchRepository
    ): SearchPlantsUseCase {
        return SearchPlantsUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePlantSearchUseCases(
        searchPlantsUseCase: SearchPlantsUseCase,
        getPlantDetailUseCase: GetPlantDetailUseCase
    ): PlantSearchUseCases {
        return PlantSearchUseCases(
            searchPlantsUseCase = searchPlantsUseCase,
            getPlantDetailUseCase = getPlantDetailUseCase
        )
    }
}
