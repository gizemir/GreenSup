package com.gizemir.plantapp.core.di

import android.content.Context
import com.gizemir.plantapp.data.local.PlantAppDatabase
import com.gizemir.plantapp.data.local.dao.favorites.FavoritePlantDao
import com.gizemir.plantapp.data.repository.favorite.FavoriteRepositoryImpl
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FavoriteModule {

    @Provides
    @Singleton
    fun provideFavoritePlantDao(database: PlantAppDatabase): FavoritePlantDao {
        return database.favoritePlantDao()
    }

    @Provides
    @Singleton
    fun provideFavoriteRepository(
        favoritePlantDao: FavoritePlantDao,
        @ApplicationContext context: Context
    ): FavoriteRepository {
        return FavoriteRepositoryImpl(favoritePlantDao, context)
    }
} 