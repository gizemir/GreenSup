package com.gizemir.plantapp.domain.use_case.weather

import com.gizemir.plantapp.domain.use_case.weather.preferences.GetLastCityUseCase
import com.gizemir.plantapp.domain.use_case.weather.preferences.SaveLastCityUseCase


data class WeatherUseCases(
    val getWeather: GetWeatherUseCase,
    val getWeekForecast: GetWeekForecastUseCase,
    val saveLastCity: SaveLastCityUseCase,
    val getLastCity: GetLastCityUseCase
)
