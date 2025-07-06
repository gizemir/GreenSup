package com.gizemir.plantapp.data.local.entity.favorites

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gizemir.plantapp.domain.model.favorite.FavoriteSource

@Entity(
    tableName = "favorite_plants",
    primaryKeys = ["userId", "plantId"]
)
data class FavoritePlantEntity(
    val userId: String,
    val plantId: Int,
    val commonName: String,
    val scientificName: String,
    val imageUrl: String?,
    val userImageUri: String?,
    val family: String?,
    val genus: String?,
    val addedAt: Long = System.currentTimeMillis(),
    val source: String = FavoriteSource.PLANT_SEARCH.name
) 