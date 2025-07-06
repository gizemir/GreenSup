package com.gizemir.plantapp.domain.use_case.plant_analysis

import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import javax.inject.Inject

class SavePlantAnalysisUseCase @Inject constructor(
    private val repository: PlantAnalysisRepository
) {
    suspend operator fun invoke(detection: DiseaseDetection, userId: String? = null) {
        repository.saveDiseaseDetection(detection, userId)
    }
} 