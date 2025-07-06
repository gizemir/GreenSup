package com.gizemir.plantapp.data.remote.dto.weather

data class WeatherForecastDto(
    val list: List<ForecastDetailDto>,
    val city: CityDto
)

data class ForecastDetailDto(
    val dt: Long,
    val main: MainWeatherData,
    val weather: List<WeatherData>,
    val dt_txt: String
)

data class CityDto(
    val name: String,
    val country: String
)

