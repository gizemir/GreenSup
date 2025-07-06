package com.gizemir.plantapp.data.local.dao.weather

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.weather.DayWeatherEntity

@Dao
interface DayWeatherDao {
    
    @Query("SELECT * FROM day_weather_cache WHERE city = :city ORDER BY date ASC")
    suspend fun getForecastByCity(city: String): List<DayWeatherEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayWeather(dayWeather: DayWeatherEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDayWeatherList(dayWeatherList: List<DayWeatherEntity>)
    
    @Query("DELETE FROM day_weather_cache WHERE city = :city")
    suspend fun deleteForecastByCity(city: String)
    
    @Query("DELETE FROM day_weather_cache WHERE cached_at < :expireTime")
    suspend fun deleteExpiredForecast(expireTime: Long)
    
    @Query("DELETE FROM day_weather_cache")
    suspend fun deleteAllForecast()
} 