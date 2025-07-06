package com.gizemir.plantapp.domain.use_case.plant_analysis

import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlantAnalysisUseCase @Inject constructor(
    private val repository: PlantAnalysisRepository
) {
    operator fun invoke(): Flow<List<DiseaseDetection>> {
        return repository.getAllDiseaseDetections()
    }
    
    operator fun invoke(userId: String): Flow<List<DiseaseDetection>> {
        return repository.getDiseaseDetectionsByUser(userId)
    }
} 