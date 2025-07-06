package com.gizemir.plantapp.data.mapper.garden

import com.gizemir.plantapp.data.local.entity.garden.GardenPlantEntity
import com.gizemir.plantapp.domain.model.garden.GardenPlant

fun GardenPlantEntity.toGardenPlant(): GardenPlant {
    return GardenPlant(
        id = id,
        userId = userId,
        plantId = plantId,
        name = name,
        imageUrl = imageUrl,
        plantedDate = plantedDate,
        lastWateredDate = lastWateredDate,
        wateringPeriodDays = wateringPeriodDays,
        notes = notes
    )
}

fun GardenPlant.toGardenPlantEntity(): GardenPlantEntity {
    return GardenPlantEntity(
        id = id,
        userId = userId,
        plantId = plantId,
        name = name,
        imageUrl = imageUrl,
        plantedDate = plantedDate,
        lastWateredDate = lastWateredDate,
        wateringPeriodDays = wateringPeriodDays,
        notes = notes
    )
}