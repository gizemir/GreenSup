package com.gizemir.plantapp.data.local.dao.favorites

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.favorites.FavoritePlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePlantDao {
    
    @Query("SELECT * FROM favorite_plants WHERE userId = :userId ORDER BY addedAt DESC")
    fun getFavoritesByUser(userId: String): Flow<List<FavoritePlantEntity>>
    
    @Query("SELECT * FROM favorite_plants ORDER BY addedAt DESC")
    fun getAllFavoritePlants(): Flow<List<FavoritePlantEntity>>
    
    @Query("SELECT * FROM favorite_plants WHERE userId = :userId AND plantId = :plantId")
    suspend fun getFavoritePlantById(userId: String, plantId: Int): FavoritePlantEntity?
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_plants WHERE userId = :userId AND plantId = :plantId)")
    suspend fun isFavorite(userId: String, plantId: Int): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoritePlant(plant: FavoritePlantEntity)
    
    @Delete
    suspend fun deleteFavoritePlant(plant: FavoritePlantEntity)
    
    @Query("DELETE FROM favorite_plants WHERE userId = :userId AND plantId = :plantId")
    suspend fun deleteFavoritePlantById(userId: String, plantId: Int)
    
    @Query("DELETE FROM favorite_plants WHERE userId = :userId")
    suspend fun deleteAllFavoritesByUser(userId: String)
    
    @Query("DELETE FROM favorite_plants")
    suspend fun deleteAllFavorites()
    
    @Query("SELECT COUNT(*) FROM favorite_plants WHERE userId = :userId")
    suspend fun getFavoriteCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM favorite_plants")
    suspend fun getFavoriteCount(): Int
} 