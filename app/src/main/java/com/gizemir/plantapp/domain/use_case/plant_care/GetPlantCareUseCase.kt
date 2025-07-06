package com.gizemir.plantapp.domain.use_case.plant_care

import com.gizemir.plantapp.domain.model.plant_care.PlantCare
import com.gizemir.plantapp.domain.repository.plant_care.PlantCareRepository
import javax.inject.Inject

class GetPlantCareUseCase @Inject constructor(
    private val repository: PlantCareRepository
) {
    suspend operator fun invoke(plantName: String, scientificName: String): Result<PlantCare> {
        return repository.getPlantCareInfo(plantName, scientificName)
    }
} 