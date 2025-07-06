package com.gizemir.plantapp.domain.repository.plant_care

import com.gizemir.plantapp.domain.model.plant_care.PlantCare

interface PlantCareRepository {
    suspend fun getPlantCareInfo(plantName: String, scientificName: String): Result<PlantCare>
} 