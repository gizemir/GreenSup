package com.gizemir.plantapp.data.mapper.weather

import com.gizemir.plantapp.data.remote.dto.weather.WeatherDto
import com.gizemir.plantapp.domain.model.weather.Weather

fun WeatherDto.toDomainModel(): Weather {
    return Weather(
        temperature = "${main.temp.toInt()}°C",
        city = name ?: "Unknown City",
        description = weather.firstOrNull()?.description?.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase() else it.toString() 
        } ?: "No description",
        iconUrl = "https://openweathermap.org/img/wn/${weather.firstOrNull()?.icon ?: "01d"}@2x.png",
        humidity = "${main.humidity}%",
        windSpeed = "${wind?.speed?.toInt() ?: 0} km/h"
    )
} 