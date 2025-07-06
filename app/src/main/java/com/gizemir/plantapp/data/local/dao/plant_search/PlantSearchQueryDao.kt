package com.gizemir.plantapp.data.local.dao.plant_search

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.plant_search.PlantSearchQueryEntity

@Dao
interface PlantSearchQueryDao {
    
    @Query("SELECT * FROM plant_search_queries WHERE query = :searchQuery AND page = :page")
    suspend fun getSearchQuery(searchQuery: String, page: Int): PlantSearchQueryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: PlantSearchQueryEntity)
    
    @Query("DELETE FROM plant_search_queries WHERE cached_at < :expireTime")
    suspend fun deleteExpiredQueries(expireTime: Long)
    
    @Query("DELETE FROM plant_search_queries")
    suspend fun deleteAllQueries()
    
    @Query("SELECT * FROM plant_search_queries ORDER BY cached_at DESC LIMIT :limit")
    suspend fun getRecentQueries(limit: Int): List<PlantSearchQueryEntity>
} 