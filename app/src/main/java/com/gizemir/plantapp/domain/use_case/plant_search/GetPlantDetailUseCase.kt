package com.gizemir.plantapp.domain.use_case.plant_search

import com.gizemir.plantapp.domain.model.plant_search.PlantDetail
import com.gizemir.plantapp.domain.repository.plant_search.PlantSearchRepository
import javax.inject.Inject

class GetPlantDetailUseCase @Inject constructor(
    private val plantSearchRepository: PlantSearchRepository
) {
    suspend operator fun invoke(plantId: Int): Result<PlantDetail> {
        if (plantId <= 0) {
            return Result.failure(IllegalArgumentException("Invalid plant ID"))
        }
        return plantSearchRepository.getPlantDetail(plantId)
    }
}
