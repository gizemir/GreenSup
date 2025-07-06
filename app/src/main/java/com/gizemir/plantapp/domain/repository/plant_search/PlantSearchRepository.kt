package com.gizemir.plantapp.domain.repository.plant_search

import com.gizemir.plantapp.domain.model.plant_search.Plant
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail

interface PlantSearchRepository {
    suspend fun searchPlants(query: String, page: Int = 1): Result<List<Plant>>
    suspend fun getPlantDetail(plantId: Int): Result<PlantDetail>
}

