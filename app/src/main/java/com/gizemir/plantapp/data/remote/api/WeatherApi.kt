package com.gizemir.plantapp.data.remote.api

import com.gizemir.plantapp.data.remote.dto.weather.ForecastResponseDto
import com.gizemir.plantapp.data.remote.dto.weather.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") city: String,
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): WeatherResponse

    @GET("data/2.5/forecast")
    suspend fun getWeeklyForecast(
        @Query("q") city: String, 
        @Query("units") units: String,
        @Query("appid") apiKey: String
    ): ForecastResponseDto
}

