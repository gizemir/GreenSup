package com.gizemir.plantapp.data.local.entity.weather

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gizemir.plantapp.domain.model.weather.DayWeather

@Entity(tableName = "day_weather_cache")
data class DayWeatherEntity(
    @PrimaryKey val id: String,
    val city: String,
    val day: String,
    val date: String,
    val iconUrl: String,
    val temperature: String,
    val humidity: String,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toDayWeather(): DayWeather {
        return DayWeather(
            day = day,
            date = date,
            iconUrl = iconUrl,
            temperature = temperature,
            humidity = humidity
        )
    }
    
    companion object {
        fun fromDayWeather(dayWeather: DayWeather, city: String): DayWeatherEntity {
            return DayWeatherEntity(
                id = "${city}_${dayWeather.date}",
                city = city,
                day = dayWeather.day,
                date = dayWeather.date,
                iconUrl = dayWeather.iconUrl,
                temperature = dayWeather.temperature,
                humidity = dayWeather.humidity
            )
        }
    }
} 