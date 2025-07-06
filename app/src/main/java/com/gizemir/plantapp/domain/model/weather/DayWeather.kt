package com.gizemir.plantapp.domain.model.weather


data class DayWeather(
    val day: String,
    val date: String,
    val iconUrl: String,
    val temperature: String,
    val humidity: String
)
