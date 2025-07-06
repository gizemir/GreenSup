package com.gizemir.plantapp.data.local.dao.plant_search

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.plant_search.PlantEntity

@Dao
interface PlantDao {
    
    @Query("SELECT * FROM plants WHERE id = :plantId")
    suspend fun getPlantById(plantId: Int): PlantEntity?
    
    @Query("SELECT * FROM plants WHERE id IN (:plantIds)")
    suspend fun getPlantsByIds(plantIds: List<Int>): List<PlantEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlants(plants: List<PlantEntity>)
    
    @Query("DELETE FROM plants WHERE cached_at < :expireTime")
    suspend fun deleteExpiredPlants(expireTime: Long)
    
    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()
    
    @Query("SELECT COUNT(*) FROM plants")
    suspend fun getPlantCount(): Int
    
    @Query("SELECT * FROM plants ORDER BY cached_at DESC LIMIT :limit")
    suspend fun getRecentPlants(limit: Int): List<PlantEntity>
} 