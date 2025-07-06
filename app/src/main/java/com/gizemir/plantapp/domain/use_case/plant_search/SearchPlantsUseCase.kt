package com.gizemir.plantapp.domain.use_case.plant_search

import com.gizemir.plantapp.domain.model.plant_search.Plant
import com.gizemir.plantapp.domain.repository.plant_search.PlantSearchRepository
import javax.inject.Inject

class SearchPlantsUseCase @Inject constructor(
    private val plantSearchRepository: PlantSearchRepository
) {
    suspend operator fun invoke(query: String, page: Int = 1): Result<List<Plant>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        return plantSearchRepository.searchPlants(query, page)
    }
}
