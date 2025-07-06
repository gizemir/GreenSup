package com.gizemir.plantapp.domain.use_case.weather.preferences

import com.gizemir.plantapp.domain.repository.weather.PreferencesRepository

class SaveLastCityUseCase(
    private val repository: PreferencesRepository
) {
    operator fun invoke(city: String) {
        if (city.isNotBlank()) {
            repository.saveLastCity(city)
        }
    }
}
