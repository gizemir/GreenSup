package com.gizemir.plantapp.domain.use_case.weather

import com.gizemir.plantapp.domain.model.weather.Weather
import com.gizemir.plantapp.domain.repository.weather.WeatherRepository
import com.gizemir.plantapp.core.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import retrofit2.HttpException

class GetWeatherUseCase(
    private val repository: WeatherRepository
) {
    operator fun invoke(city: String): Flow<Resource<Weather>> = flow {
        try {
            if (city.isBlank()) {
                emit(Resource.Error("City name cannot be empty"))
                return@flow
            }

            emit(Resource.Loading())
            
            val weather = repository.getCurrentWeather(city)
            
            emit(Resource.Success(weather))
        } catch (e: HttpException) {
            emit(Resource.Error("Network error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Connection error: Check your internet connection"))
        } catch (e: Exception) {
            emit(Resource.Error("An unexpected error occurred: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }
}

