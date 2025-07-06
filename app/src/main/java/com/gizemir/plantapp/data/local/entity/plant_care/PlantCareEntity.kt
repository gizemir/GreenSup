package com.gizemir.plantapp.data.local.entity.plant_care

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gizemir.plantapp.data.local.converter.Converters
import com.gizemir.plantapp.domain.model.plant_care.PlantCare
import com.gizemir.plantapp.domain.model.plant_care.PlantCareSection

@Entity(tableName = "plant_care")
@TypeConverters(Converters::class)
data class PlantCareEntity(
    @PrimaryKey
    val id: String,
    val plantName: String,
    val scientificName: String,

    val wateringTitle: String,
    val wateringDescription: String,
    val wateringTips: List<String>,

    val lightingTitle: String,
    val lightingDescription: String,
    val lightingTips: List<String>,

    val soilTitle: String,
    val soilDescription: String,
    val soilTips: List<String>,

    val temperatureTitle: String,
    val temperatureDescription: String,
    val temperatureTips: List<String>,

    val humidityTitle: String,
    val humidityDescription: String,
    val humidityTips: List<String>,

    val commonProblemsTitle: String,
    val commonProblemsDescription: String,
    val commonProblemsTips: List<String>,
    

    val generalTips: List<String>,
    

    val timestamp: Long = System.currentTimeMillis()
) {
    fun toPlantCare(): PlantCare {
        return PlantCare(
            plantName = plantName,
            scientificName = scientificName,
            watering = PlantCareSection(
                title = wateringTitle,
                description = wateringDescription,
                tips = wateringTips
            ),
            lighting = PlantCareSection(
                title = lightingTitle,
                description = lightingDescription,
                tips = lightingTips
            ),
            soil = PlantCareSection(
                title = soilTitle,
                description = soilDescription,
                tips = soilTips
            ),
            temperature = PlantCareSection(
                title = temperatureTitle,
                description = temperatureDescription,
                tips = temperatureTips
            ),
            humidity = PlantCareSection(
                title = humidityTitle,
                description = humidityDescription,
                tips = humidityTips
            ),
            commonProblems = PlantCareSection(
                title = commonProblemsTitle,
                description = commonProblemsDescription,
                tips = commonProblemsTips
            ),
            generalTips = generalTips
        )
    }
    
    companion object {
        fun fromPlantCare(plantCare: PlantCare): PlantCareEntity {
            val id = "${plantCare.plantName.lowercase().trim()}_${plantCare.scientificName.lowercase().trim()}"
            return PlantCareEntity(
                id = id,
                plantName = plantCare.plantName,
                scientificName = plantCare.scientificName,
                wateringTitle = plantCare.watering.title,
                wateringDescription = plantCare.watering.description,
                wateringTips = plantCare.watering.tips,
                lightingTitle = plantCare.lighting.title,
                lightingDescription = plantCare.lighting.description,
                lightingTips = plantCare.lighting.tips,
                soilTitle = plantCare.soil.title,
                soilDescription = plantCare.soil.description,
                soilTips = plantCare.soil.tips,
                temperatureTitle = plantCare.temperature.title,
                temperatureDescription = plantCare.temperature.description,
                temperatureTips = plantCare.temperature.tips,
                humidityTitle = plantCare.humidity.title,
                humidityDescription = plantCare.humidity.description,
                humidityTips = plantCare.humidity.tips,
                commonProblemsTitle = plantCare.commonProblems.title,
                commonProblemsDescription = plantCare.commonProblems.description,
                commonProblemsTips = plantCare.commonProblems.tips,
                generalTips = plantCare.generalTips
            )
        }
    }
} 