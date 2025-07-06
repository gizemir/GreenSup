package com.gizemir.plantapp.data.local.dao.plant_search

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.plant_search.PlantDetailEntity

@Dao
interface PlantDetailDao {
    
    @Query("SELECT * FROM plant_details WHERE id = :plantId")
    suspend fun getPlantDetailById(plantId: Int): PlantDetailEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantDetail(plantDetail: PlantDetailEntity)
    
    @Query("DELETE FROM plant_details WHERE cached_at < :expireTime")
    suspend fun deleteExpiredPlantDetails(expireTime: Long)
    
    @Query("DELETE FROM plant_details")
    suspend fun deleteAllPlantDetails()
    
    @Query("SELECT COUNT(*) FROM plant_details")
    suspend fun getPlantDetailCount(): Int
    
    @Query("SELECT * FROM plant_details ORDER BY cached_at DESC LIMIT :limit")
    suspend fun getRecentPlantDetails(limit: Int): List<PlantDetailEntity>
} 