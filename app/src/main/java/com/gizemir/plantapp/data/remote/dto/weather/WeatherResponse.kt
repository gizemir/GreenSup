package com.gizemir.plantapp.data.remote.dto.weather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("name") val name: String,
    @SerializedName("main") val main: MainWeatherData,
    @SerializedName("weather") val weather: List<WeatherData>,
    @SerializedName("wind") val wind: WindData
)

data class MainWeatherData(
    @SerializedName("temp") val temp: Double,
    @SerializedName("humidity") val humidity: Int
)

data class WeatherData(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class WindData(
    @SerializedName("speed") val speed: Double
)

data class ForecastResponseDto(
    @SerializedName("list") val list: List<ForecastItemResponseDto>,
    @SerializedName("city") val city: ForecastCityData
)

data class ForecastItemResponseDto(
    @SerializedName("dt_txt") val dateText: String,
    @SerializedName("main") val main: MainWeatherData,
    @SerializedName("weather") val weather: List<WeatherData>,
    @SerializedName("wind") val wind: WindData?
)

data class ForecastCityData(
    @SerializedName("name") val name: String
)
