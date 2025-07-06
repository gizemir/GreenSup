package com.gizemir.plantapp.domain.repository.plant_analysis

import android.net.Uri
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface PlantAnalysisRepository {
    
    suspend fun detectDisease(
        imageUri: Uri
    ): Resource<DiseaseDetection>
    
    suspend fun identifyPlantOnly(
        imageUri: Uri
    ): Resource<DiseaseDetection>
    
    suspend fun detectDiseaseOnly(
        imageUri: Uri
    ): Resource<DiseaseDetection>
    
    suspend fun saveDiseaseDetection(detection: DiseaseDetection, userId: String? = null)
    
    suspend fun getDiseaseDetectionById(id: String): DiseaseDetection?
    
    fun getAllDiseaseDetections(): Flow<List<DiseaseDetection>>
    
    fun getDiseaseDetectionsByUser(userId: String): Flow<List<DiseaseDetection>>
    
    suspend fun deleteDiseaseDetection(id: String)
    
    suspend fun deleteAllDiseaseDetections()
    
    suspend fun clearAllDiseaseDetections(userId: String? = null)
} 