package com.gizemir.plantapp.domain.use_case.weather.preferences

import com.gizemir.plantapp.domain.repository.weather.PreferencesRepository

class GetLastCityUseCase(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): String {
        return repository.getLastCity()
    }
}
