package com.gizemir.plantapp.domain.use_case.plant_analysis

import android.net.Uri
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import com.gizemir.plantapp.domain.util.Resource
import javax.inject.Inject

class PlantAnalysisUseCase @Inject constructor(
    private val repository: PlantAnalysisRepository
) {
    suspend operator fun invoke(imageUri: Uri): Resource<DiseaseDetection> {
        return repository.detectDisease(imageUri)
    }
    
    suspend fun identifyPlantOnly(imageUri: Uri): Resource<DiseaseDetection> {
        return repository.identifyPlantOnly(imageUri)
    }
    
    suspend fun detectDiseaseOnly(imageUri: Uri): Resource<DiseaseDetection> {
        return repository.detectDiseaseOnly(imageUri)
    }
} 