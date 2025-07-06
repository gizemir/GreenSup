package com.gizemir.plantapp.domain.repository.favorite

import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    
    fun getFavoritesByUser(userId: String): Flow<List<FavoritePlant>>
    
    fun getAllFavorites(): Flow<List<FavoritePlant>>
    
    suspend fun getFavoriteById(userId: String, plantId: Int): FavoritePlant?
    
    suspend fun isFavorite(userId: String, plantId: Int): Boolean
    
    suspend fun addToFavorites(plant: FavoritePlant)
    
    suspend fun removeFromFavorites(userId: String, plantId: Int)
    
    suspend fun getFavoriteCount(userId: String): Int
    
    suspend fun clearAllFavorites()
    
    suspend fun clearAllFavoritesByUser(userId: String)
    
    suspend fun migrateUnknownUserFavorites()
} 