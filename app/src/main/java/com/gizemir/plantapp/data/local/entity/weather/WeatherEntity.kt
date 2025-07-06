package com.gizemir.plantapp.data.local.entity.weather

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gizemir.plantapp.domain.model.weather.Weather

@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey val city: String,
    val temperature: String,
    val description: String,
    val iconUrl: String,
    val humidity: String,
    val windSpeed: String,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toWeather(): Weather {
        return Weather(
            city = city,
            temperature = temperature,
            description = description,
            iconUrl = iconUrl,
            humidity = humidity,
            windSpeed = windSpeed
        )
    }
    
    companion object {
        fun fromWeather(weather: Weather): WeatherEntity {
            return WeatherEntity(
                city = weather.city,
                temperature = weather.temperature,
                description = weather.description,
                iconUrl = weather.iconUrl,
                humidity = weather.humidity,
                windSpeed = weather.windSpeed
            )
        }
    }
} 