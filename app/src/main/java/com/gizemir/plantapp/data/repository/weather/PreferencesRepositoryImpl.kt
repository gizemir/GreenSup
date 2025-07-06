package com.gizemir.plantapp.data.repository.weather

import android.content.SharedPreferences
import android.util.Log
import com.gizemir.plantapp.domain.repository.weather.PreferencesRepository
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : PreferencesRepository {
    
    companion object {
        private const val KEY_LAST_CITY = "last_city"
        private const val DEFAULT_CITY = "Istanbul"
    }
    
    override fun getLastCity(): String {
        val city = sharedPreferences.getString(KEY_LAST_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
        Log.d("PreferencesRepo", "Retrieved last city: $city")
        return city
    }
    
    override fun saveLastCity(city: String) {
        if (city.isBlank()) return
        Log.d("PreferencesRepo", "Saving city: $city")
        sharedPreferences.edit().putString(KEY_LAST_CITY, city).apply()
    }
}
