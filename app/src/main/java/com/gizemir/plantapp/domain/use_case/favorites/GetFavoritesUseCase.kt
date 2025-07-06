package com.gizemir.plantapp.domain.use_case.favorites

import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<FavoritePlant>> {
        return repository.getAllFavorites()
    }
    
    operator fun invoke(userId: String): Flow<List<FavoritePlant>> {
        return repository.getFavoritesByUser(userId)
    }
}

