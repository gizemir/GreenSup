package com.gizemir.plantapp.data.local.dao.plant_analysis

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.plant_analysis.DiseaseDetectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantAnalysisDao {
    
    @Query("SELECT * FROM disease_detections ORDER BY analyzedAt DESC")
    fun getAllDiseaseDetections(): Flow<List<DiseaseDetectionEntity>>
    
    @Query("SELECT * FROM disease_detections WHERE userId = :userId ORDER BY analyzedAt DESC")
    fun getDiseaseDetectionsByUser(userId: String): Flow<List<DiseaseDetectionEntity>>
    
    @Query("SELECT * FROM disease_detections WHERE id = :id LIMIT 1")
    suspend fun getDiseaseDetectionById(id: String): DiseaseDetectionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiseaseDetection(detection: DiseaseDetectionEntity)
    
    @Delete
    suspend fun deleteDiseaseDetection(detection: DiseaseDetectionEntity)
    
    @Query("DELETE FROM disease_detections WHERE id = :id")
    suspend fun deleteDiseaseDetectionById(id: String)
    
    @Query("DELETE FROM disease_detections")
    suspend fun deleteAllDiseaseDetections()
    
    @Query("DELETE FROM disease_detections WHERE userId = :userId")
    suspend fun deleteAllDiseaseDetectionsByUser(userId: String)
    
    @Query("DELETE FROM disease_detections WHERE analyzedAt < :timestamp")
    suspend fun deleteOldDiseaseDetections(timestamp: Long)
} 