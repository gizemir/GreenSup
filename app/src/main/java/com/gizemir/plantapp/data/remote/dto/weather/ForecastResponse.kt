package com.gizemir.plantapp.data.remote.dto.weather

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    val city: CityData,
    val list: List<ForecastItem>
)

data class CityData(
    val name: String,
    val country: String
)

data class ForecastItem(
    @SerializedName("dt") val date: Long,
    val main: MainWeatherData,
    val weather: List<WeatherData>,
    @SerializedName("dt_txt") val dateText: String
)
