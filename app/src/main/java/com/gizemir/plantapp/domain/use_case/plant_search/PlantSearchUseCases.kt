package com.gizemir.plantapp.domain.use_case.plant_search

import javax.inject.Inject

data class PlantSearchUseCases @Inject constructor(
    val searchPlantsUseCase: SearchPlantsUseCase,
    val getPlantDetailUseCase: GetPlantDetailUseCase
)
