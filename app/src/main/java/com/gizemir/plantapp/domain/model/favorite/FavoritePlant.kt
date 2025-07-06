package com.gizemir.plantapp.domain.model.favorite

data class FavoritePlant(
    val userId: String,
    val plantId: Int,
    val commonName: String,
    val scientificName: String,
    val imageUrl: String?,
    val userImageUri: String?,
    val family: String?,
    val genus: String?,
    val addedAt: Long,
    val source: FavoriteSource = FavoriteSource.PLANT_SEARCH
) 