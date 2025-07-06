package com.gizemir.plantapp.data.local.dao.plant_care

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.plant_care.PlantCareEntity

@Dao
interface PlantCareDao {
    
    @Query("SELECT * FROM plant_care WHERE id = :id")
    suspend fun getPlantCareById(id: String): PlantCareEntity?
    
    @Query("SELECT * FROM plant_care WHERE plantName = :plantName AND scientificName = :scientificName")
    suspend fun getPlantCare(plantName: String, scientificName: String): PlantCareEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantCare(plantCare: PlantCareEntity)
    
    @Update
    suspend fun updatePlantCare(plantCare: PlantCareEntity)
    
    @Delete
    suspend fun deletePlantCare(plantCare: PlantCareEntity)
    
    @Query("DELETE FROM plant_care WHERE id = :id")
    suspend fun deletePlantCareById(id: String)
    
    @Query("DELETE FROM plant_care WHERE timestamp < :expireTime")
    suspend fun deleteExpiredPlantCare(expireTime: Long)
    
    @Query("DELETE FROM plant_care")
    suspend fun deleteAllPlantCare()
    
    @Query("SELECT COUNT(*) FROM plant_care")
    suspend fun getPlantCareCount(): Int
    
    @Query("SELECT * FROM plant_care ORDER BY timestamp DESC")
    suspend fun getAllPlantCare(): List<PlantCareEntity>
    
    @Query("SELECT * FROM plant_care WHERE timestamp > :timestamp ORDER BY timestamp DESC")
    suspend fun getRecentPlantCare(timestamp: Long): List<PlantCareEntity>
    
    @Query("SELECT * FROM plant_care WHERE plantName LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%'")
    suspend fun searchPlantCare(query: String): List<PlantCareEntity>
} 