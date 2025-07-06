package com.gizemir.plantapp.data.repository.weather

import android.util.Log
import com.gizemir.plantapp.data.local.dao.weather.WeatherDao
import com.gizemir.plantapp.data.local.dao.weather.DayWeatherDao
import com.gizemir.plantapp.data.local.entity.weather.WeatherEntity
import com.gizemir.plantapp.data.local.entity.weather.DayWeatherEntity
import com.gizemir.plantapp.data.remote.api.WeatherApi
import com.gizemir.plantapp.domain.model.weather.DayWeather
import com.gizemir.plantapp.domain.model.weather.Weather
import com.gizemir.plantapp.domain.repository.weather.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val apiKey: String,
    private val weatherDao: WeatherDao,
    private val dayWeatherDao: DayWeatherDao
) : WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"
        private const val CACHE_EXPIRY_TIME = 30 * 60 * 1000L // 30 dakika
    }

    override suspend fun getCurrentWeather(city: String): Weather {
        return withContext(Dispatchers.IO) {
            try {
                val cachedWeather = weatherDao.getWeatherByCity(city)
                val currentTime = System.currentTimeMillis()
                
                if (cachedWeather != null && (currentTime - cachedWeather.cached_at) < CACHE_EXPIRY_TIME) {
                    Log.d(TAG, "Cache'den hava durumu döndürülüyor - City: $city")
                    return@withContext cachedWeather.toWeather()
                }
                
                Log.d(TAG, "API'den hava durumu getiriliyor - City: $city")
                
                val response = api.getCurrentWeather(city, "metric", apiKey)
                Log.d(TAG, "API response for current weather: $response")
                
                val weather = Weather(
                    city = response.name,
                    temperature = "${response.main.temp.toInt()}°C",
                    description = response.weather.firstOrNull()?.description ?: "Unknown",
                    iconUrl = formatIconUrl(response.weather.firstOrNull()?.icon ?: "01d"),
                    humidity = "${response.main.humidity}%",
                    windSpeed = "${response.wind.speed} km/h"
                )
                
                val weatherEntity = WeatherEntity.fromWeather(weather)
                weatherDao.insertWeather(weatherEntity)
                Log.d(TAG, "Hava durumu cache'e kaydedildi - City: $city")
                
                weather
            } catch (e: HttpException) {
                Log.e(TAG, "HTTP error getting current weather: ${e.message ?: "Unknown HTTP error"}")
                
                val cachedWeather = weatherDao.getWeatherByCity(city)
                if (cachedWeather != null) {
                    Log.d(TAG, "API hatası, cache'den eski veri döndürülüyor - City: $city")
                    return@withContext cachedWeather.toWeather()
                }
                
                throw e
            } catch (e: IOException) {
                Log.e(TAG, "IO error getting current weather: ${e.message ?: "Unknown IO error"}")
                
                val cachedWeather = weatherDao.getWeatherByCity(city)
                if (cachedWeather != null) {
                    Log.d(TAG, "Network hatası, cache'den eski veri döndürülüyor - City: $city")
                    return@withContext cachedWeather.toWeather()
                }
                
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error getting current weather: ${e.message ?: "Unknown error"}")
                
                val cachedWeather = weatherDao.getWeatherByCity(city)
                if (cachedWeather != null) {
                    Log.d(TAG, "Genel hata, cache'den eski veri döndürülüyor - City: $city")
                    return@withContext cachedWeather.toWeather()
                }
                
                throw e
            }
        }
    }

    override suspend fun getWeekForecast(city: String): List<DayWeather> {
        return withContext(Dispatchers.IO) {
            try {
                val cachedForecast = dayWeatherDao.getForecastByCity(city)
                val currentTime = System.currentTimeMillis()
                
                if (cachedForecast.isNotEmpty() && 
                    cachedForecast.all { (currentTime - it.cached_at) < CACHE_EXPIRY_TIME }) {
                    Log.d(TAG, "Cache'den haftalık tahmin döndürülüyor - City: $city")
                    return@withContext cachedForecast.map { it.toDayWeather() }
                }
                
                Log.d(TAG, "API'den haftalık tahmin getiriliyor - City: $city")
                
                val maskedApiKey = if (apiKey.length > 5) "${apiKey.take(3)}...${apiKey.takeLast(2)}" else "***"
                Log.d(TAG, "Fetching forecast for city: $city with api key: $maskedApiKey")
                
                val response = api.getWeeklyForecast(city, "metric", apiKey)
                
                if (response.list.isEmpty()) {
                    Log.w(TAG, "Empty forecast list for city: $city")
                    
                    if (cachedForecast.isNotEmpty()) {
                        Log.d(TAG, "Boş response, cache'den eski veri döndürülüyor - City: $city")
                        return@withContext cachedForecast.map { it.toDayWeather() }
                    }
                    
                    return@withContext emptyList()
                }
                
                Log.d(TAG, "Received ${response.list.size} forecast items for city: $city")
                
                val dayWeatherList = response.list
                    .groupBy { it.dateText.split(" ")[0] }
                    .map { (_, items) -> items.first() }
                    .take(5)
                    .mapIndexed { index, forecastItem ->
                        val date = forecastItem.dateText.split(" ")[0]
                        Log.d(TAG, "Processing day $index: $date")
                        
                        DayWeather(
                            day = getDayOfWeek(date),
                            date = formatDate(date),
                            temperature = "${forecastItem.main.temp.toInt()}°C",
                            humidity = "${forecastItem.main.humidity}%",
                            iconUrl = formatIconUrl(forecastItem.weather.firstOrNull()?.icon ?: "01d")
                        )
                    }
                
                if (dayWeatherList.isNotEmpty()) {
                    dayWeatherDao.deleteForecastByCity(city)
                    
                    val entities = dayWeatherList.map { DayWeatherEntity.fromDayWeather(it, city) }
                    dayWeatherDao.insertDayWeatherList(entities)
                    Log.d(TAG, "${dayWeatherList.size} günlük tahmin cache'e kaydedildi - City: $city")
                }
                
                dayWeatherList
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching forecast", e)
                
                val cachedForecast = dayWeatherDao.getForecastByCity(city)
                if (cachedForecast.isNotEmpty()) {
                    Log.d(TAG, "API hatası, cache'den eski tahmin döndürülüyor - City: $city")
                    return@withContext cachedForecast.map { it.toDayWeather() }
                }
                
                when (e) {
                    is HttpException -> {
                        val code = e.code()
                        val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
                        Log.e(TAG, "HTTP $code error: $errorBody")
                    }
                    is IOException -> Log.e(TAG, "Network error: ${e.message}")
                }
                emptyList()
            }
        }
    }
    
    suspend fun clearExpiredCache() {
        try {
            val expireTime = System.currentTimeMillis() - CACHE_EXPIRY_TIME
            weatherDao.deleteExpiredWeather(expireTime)
            dayWeatherDao.deleteExpiredForecast(expireTime)
            Log.d(TAG, "Süresi dolmuş weather cache temizlendi")
        } catch (e: Exception) {
            Log.e(TAG, "Weather cache temizleme hatası: ${e.message}")
        }
    }
    
    suspend fun clearAllCache() {
        try {
            weatherDao.deleteAllWeather()
            dayWeatherDao.deleteAllForecast()
            Log.d(TAG, "Tüm weather cache temizlendi")
        } catch (e: Exception) {
            Log.e(TAG, "Weather cache temizleme hatası: ${e.message}")
        }
    }
    
    private fun formatIconUrl(iconCode: String): String = "https://openweathermap.org/img/wn/$iconCode@4x.png"
    
    private fun getDayOfWeek(dateString: String): String {

        val dateParts = dateString.split("-")
        if (dateParts.size != 3) return "Unknown"
        
        val year = dateParts[0].toIntOrNull() ?: return "Unknown"
        val month = dateParts[1].toIntOrNull()?.minus(1) ?: return "Unknown" // Month is 0-based
        val day = dateParts[2].toIntOrNull() ?: return "Unknown"
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month, day)
        
        return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "Monday"
            java.util.Calendar.TUESDAY -> "Tuesday"
            java.util.Calendar.WEDNESDAY -> "Wednesday"
            java.util.Calendar.THURSDAY -> "Thursday"
            java.util.Calendar.FRIDAY -> "Friday"
            java.util.Calendar.SATURDAY -> "Saturday"
            java.util.Calendar.SUNDAY -> "Sunday"
            else -> "Unknown"
        }
    }
    
    private fun formatDate(dateString: String): String {
        val dateParts = dateString.split("-")
        if (dateParts.size != 3) return dateString
        
        val year = dateParts[0].toIntOrNull() ?: return dateString
        val month = dateParts[1].toIntOrNull()?.minus(1) ?: return dateString // Month is 0-based
        val day = dateParts[2].toIntOrNull() ?: return dateString
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month, day)
        
        val monthName = when (calendar.get(java.util.Calendar.MONTH)) {
            0 -> "Jan"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Apr"
            4 -> "May"
            5 -> "Jun"
            6 -> "Jul"
            7 -> "Aug"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Nov"
            11 -> "Dec"
            else -> "Unknown"
        }
        
        return "$monthName ${calendar.get(java.util.Calendar.DAY_OF_MONTH)}"
    }
}
