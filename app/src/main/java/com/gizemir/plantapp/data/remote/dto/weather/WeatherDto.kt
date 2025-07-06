package com.gizemir.plantapp.data.remote.dto.weather

data class WeatherDto(
    val main: MainDto,
    val weather: List<WeatherDescriptionDto>,
    val wind: WindDto? = null,
    val name: String? = null
)

data class MainDto(
    val temp: Double,
    val humidity: Int
)

data class WindDto(
    val speed: Double
)

data class WeatherDescriptionDto(
    val description: String,
    val icon: String
)

