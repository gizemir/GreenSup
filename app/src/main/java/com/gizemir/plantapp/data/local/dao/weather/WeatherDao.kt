package com.gizemir.plantapp.data.local.dao.weather

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.weather.WeatherEntity

@Dao
interface WeatherDao {
    
    @Query("SELECT * FROM weather_cache WHERE city = :city")
    suspend fun getWeatherByCity(city: String): WeatherEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)
    
    @Query("DELETE FROM weather_cache WHERE cached_at < :expireTime")
    suspend fun deleteExpiredWeather(expireTime: Long)
    
    @Query("DELETE FROM weather_cache")
    suspend fun deleteAllWeather()
    
    @Query("SELECT * FROM weather_cache ORDER BY cached_at DESC LIMIT :limit")
    suspend fun getRecentWeather(limit: Int): List<WeatherEntity>
} 