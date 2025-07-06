package com.gizemir.plantapp.domain.repository.weather

import com.gizemir.plantapp.domain.model.weather.Weather
import com.gizemir.plantapp.domain.model.weather.DayWeather

interface WeatherRepository {
    suspend fun getCurrentWeather(city: String): Weather
    suspend fun getWeekForecast(city: String): List<DayWeather>
}
