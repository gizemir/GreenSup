package com.gizemir.plantapp.presentation.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.repository.weather.PreferencesRepository
import com.gizemir.plantapp.domain.repository.weather.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gizemir.plantapp.domain.model.weather.DayWeather

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    private val _weekWeather = MutableStateFlow<List<DayWeather>>(emptyList())
    val weekWeather: StateFlow<List<DayWeather>> = _weekWeather

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun getWeekWeather(city: String) {
        val cityToUse = city.trim().ifBlank { "Istanbul" }
        
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Fetching 5-day forecast: $cityToUse")
                val domainForecast = weatherRepository.getWeekForecast(cityToUse)
                
                if (domainForecast.isEmpty()) {
                    _error.value = "No weather information found for $cityToUse"
                    Log.e("WeatherViewModel", "Empty forecast data returned for $cityToUse")
                } else {
                    val presentationForecast = domainForecast.map { domainDay ->
                        DayWeather(
                            day = domainDay.day,
                            date = domainDay.date,
                            iconUrl = domainDay.iconUrl,
                            temperature = domainDay.temperature,
                            humidity = domainDay.humidity
                        )
                    }
                    _weekWeather.value = presentationForecast
                    
                    Log.d("WeatherViewModel", "Received forecast with ${presentationForecast.size} days")
                    
                    saveCity(cityToUse)
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching forecast: ${e.message}", e)
                _error.value = "Could not get weather: ${e.localizedMessage}"
                _weekWeather.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
    

    fun saveCity(city: String) {
        if (city.isNotBlank()) {
            preferencesRepository.saveLastCity(city)
            Log.d("WeatherViewModel", "Saved city: $city")
        }
    }

    fun clearWeatherData() {
        _weekWeather.value = emptyList()
    }

    fun clearError() {
        _error.value = null
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
}

