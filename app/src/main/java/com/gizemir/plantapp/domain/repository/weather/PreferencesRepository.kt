package com.gizemir.plantapp.domain.repository.weather


interface PreferencesRepository {

    fun getLastCity(): String

    fun saveLastCity(city: String)
}
