package com.gizemir.plantapp.domain.model.weather

data class Weather(
    val temperature: String,
    val city: String,
    val description: String,
    val iconUrl: String,
    val humidity: String,
    val windSpeed: String
)

