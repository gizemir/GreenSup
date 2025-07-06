package com.gizemir.plantapp.data.repository.favorite

import android.content.Context
import android.net.Uri
import com.gizemir.plantapp.core.util.ImageUtils
import com.gizemir.plantapp.data.local.dao.favorites.FavoritePlantDao
import com.gizemir.plantapp.data.local.entity.favorites.FavoritePlantEntity
import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteRepositoryImpl @Inject constructor(
    private val favoritePlantDao: FavoritePlantDao,
    @ApplicationContext private val context: Context
) : FavoriteRepository {

    override fun getFavoritesByUser(userId: String): Flow<List<FavoritePlant>> {
        return favoritePlantDao.getFavoritesByUser(userId).map { entities ->
            android.util.Log.d("FavoriteRepository", "=== Loading favorites for user: $userId ===")
            android.util.Log.d("FavoriteRepository", "Found ${entities.size} favorite plants")
            
            entities.mapIndexed { index, entity ->
                android.util.Log.d("FavoriteRepository", "Plant $index:")
                android.util.Log.d("FavoriteRepository", "  UserId: ${entity.userId}")
                android.util.Log.d("FavoriteRepository", "  PlantId: ${entity.plantId}")
                android.util.Log.d("FavoriteRepository", "  CommonName: ${entity.commonName}")
                
                val domainModel = entity.toDomainModel()
                android.util.Log.d("FavoriteRepository", "  Domain UserImageUri: '${domainModel.userImageUri}'")
                
                domainModel
            }
        }
    }

    override fun getAllFavorites(): Flow<List<FavoritePlant>> {
        return favoritePlantDao.getAllFavoritePlants().map { entities ->
            android.util.Log.d("FavoriteRepository", "=== Loading all favorites from database ===")
            android.util.Log.d("FavoriteRepository", "Found ${entities.size} favorite plants")
            
            entities.mapIndexed { index, entity ->
                android.util.Log.d("FavoriteRepository", "Plant $index:")
                android.util.Log.d("FavoriteRepository", "  UserId: ${entity.userId}")
                android.util.Log.d("FavoriteRepository", "  PlantId: ${entity.plantId}")
                android.util.Log.d("FavoriteRepository", "  CommonName: ${entity.commonName}")
                
                val domainModel = entity.toDomainModel()
                
                domainModel
            }
        }
    }

    override suspend fun getFavoriteById(userId: String, plantId: Int): FavoritePlant? {
        return favoritePlantDao.getFavoritePlantById(userId, plantId)?.toDomainModel()
    }

    override suspend fun isFavorite(userId: String, plantId: Int): Boolean {
        return favoritePlantDao.isFavorite(userId, plantId)
    }

    override suspend fun addToFavorites(plant: FavoritePlant) {
        android.util.Log.d("FavoriteRepository", "=== Adding plant to database ===")
        android.util.Log.d("FavoriteRepository", "UserId: ${plant.userId}")
        android.util.Log.d("FavoriteRepository", "PlantId: ${plant.plantId}")
        android.util.Log.d("FavoriteRepository", "CommonName: ${plant.commonName}")
        android.util.Log.d("FavoriteRepository", "Source: ${plant.source}")
        
        val entity = plant.toEntity()
        android.util.Log.d("FavoriteRepository", "Entity created successfully")
        
        favoritePlantDao.insertFavoritePlant(entity)
        android.util.Log.d("FavoriteRepository", "Plant successfully saved to database")
    }

    override suspend fun removeFromFavorites(userId: String, plantId: Int) {
        favoritePlantDao.deleteFavoritePlantById(userId, plantId)
    }

    override suspend fun getFavoriteCount(userId: String): Int {
        return favoritePlantDao.getFavoriteCountByUser(userId)
    }

    override suspend fun clearAllFavorites() {
        favoritePlantDao.deleteAllFavorites()
    }

    override suspend fun clearAllFavoritesByUser(userId: String) {
        favoritePlantDao.deleteAllFavoritesByUser(userId)
    }

    override suspend fun migrateUnknownUserFavorites() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val unknownUserFavorites = favoritePlantDao.getFavoritesByUser("unknown_user")
                unknownUserFavorites.collect { entities ->
                    if (entities.isNotEmpty()) {
                        android.util.Log.d("FavoriteRepository", "Migrating ${entities.size} favorites from unknown_user to ${currentUser.uid}")
                        
                        entities.forEach { entity ->
                            val migratedEntity = entity.copy(userId = currentUser.uid)
                            favoritePlantDao.insertFavoritePlant(migratedEntity)
                            
                            favoritePlantDao.deleteFavoritePlantById("unknown_user", entity.plantId)
                        }
                        
                        android.util.Log.d("FavoriteRepository", "Migration completed successfully")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FavoriteRepository", "Error during migration: ${e.message}")
        }
    }

    private fun FavoritePlantEntity.toDomainModel(): FavoritePlant {
        return FavoritePlant(
            userId = userId,
            plantId = plantId,
            commonName = commonName,
            scientificName = scientificName,
            imageUrl = imageUrl,
            userImageUri = userImageUri,
            family = family,
            genus = genus,
            addedAt = addedAt,
            source = try {
                com.gizemir.plantapp.domain.model.favorite.FavoriteSource.valueOf(source)
            } catch (e: Exception) {
                com.gizemir.plantapp.domain.model.favorite.FavoriteSource.PLANT_SEARCH
            }
        )
    }

    private fun FavoritePlant.toEntity(): FavoritePlantEntity {
        return FavoritePlantEntity(
            userId = userId,
            plantId = plantId,
            commonName = commonName,
            scientificName = scientificName,
            imageUrl = imageUrl,
            userImageUri = userImageUri,
            family = family,
            genus = genus,
            addedAt = addedAt,
            source = source.name
        )
    }
} 