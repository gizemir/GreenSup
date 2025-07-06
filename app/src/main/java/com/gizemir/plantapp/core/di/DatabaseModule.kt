package com.gizemir.plantapp.core.di

import android.content.Context
import androidx.room.Room
import com.gizemir.plantapp.data.local.PlantAppDatabase

import com.gizemir.plantapp.data.local.dao.plant_search.PlantDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantDetailDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantSearchQueryDao
import com.gizemir.plantapp.data.local.dao.weather.WeatherDao
import com.gizemir.plantapp.data.local.dao.weather.DayWeatherDao
import com.gizemir.plantapp.data.local.dao.forum.PostDao
import com.gizemir.plantapp.data.local.dao.forum.CommentDao
import com.gizemir.plantapp.data.local.dao.plant_care.PlantCareDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlantAppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            PlantAppDatabase::class.java,
            "plant_app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun providePlantDao(database: PlantAppDatabase): PlantDao {
        return database.plantDao()
    }
    
    @Provides
    fun providePlantDetailDao(database: PlantAppDatabase): PlantDetailDao {
        return database.plantDetailDao()
    }
    
    @Provides
    fun providePlantSearchQueryDao(database: PlantAppDatabase): PlantSearchQueryDao {
        return database.plantSearchQueryDao()
    }
    
    @Provides
    fun provideWeatherDao(database: PlantAppDatabase): WeatherDao {
        return database.weatherDao()
    }
    
    @Provides
    fun provideDayWeatherDao(database: PlantAppDatabase): DayWeatherDao {
        return database.dayWeatherDao()
    }
    
    @Provides
    fun providePostDao(database: PlantAppDatabase): PostDao {
        return database.postDao()
    }
    
    @Provides
    fun provideCommentDao(database: PlantAppDatabase): CommentDao {
        return database.commentDao()
    }
    

    
    @Provides
    fun providePlantCareDao(database: PlantAppDatabase): PlantCareDao {
        return database.plantCareDao()
    }
} 