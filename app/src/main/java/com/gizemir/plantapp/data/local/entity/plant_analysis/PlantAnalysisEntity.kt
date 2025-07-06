package com.gizemir.plantapp.data.local.entity.plant_analysis

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gizemir.plantapp.data.local.converter.Converters
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.model.plant_analysis.Disease
import com.gizemir.plantapp.domain.model.plant_analysis.PlantSuggestion

@Entity(tableName = "disease_detections")
@TypeConverters(Converters::class)
data class DiseaseDetectionEntity(
    @PrimaryKey val id: String,
    val imageUri: String,
    val isPlant: Boolean,
    val isHealthy: Boolean,
    val plantProbability: Double,
    val healthyProbability: Double,
    val diseases: List<Disease>,
    val plantSuggestions: List<PlantSuggestion>,
    val analyzedAt: Long,
    val userId: String? = null
) {
    fun toDomainModel(): DiseaseDetection {
        return DiseaseDetection(
            id = id,
            imageUri = imageUri,
            isPlant = isPlant,
            isHealthy = isHealthy,
            plantProbability = plantProbability,
            healthyProbability = healthyProbability,
            diseases = diseases,
            plantSuggestions = plantSuggestions,
            analyzedAt = analyzedAt
        )
    }
}

fun DiseaseDetection.toEntity(userId: String? = null): DiseaseDetectionEntity {
    return DiseaseDetectionEntity(
        id = id,
        imageUri = imageUri,
        isPlant = isPlant,
        isHealthy = isHealthy,
        plantProbability = plantProbability,
        healthyProbability = healthyProbability,
        diseases = diseases,
        plantSuggestions = plantSuggestions,
        analyzedAt = analyzedAt,
        userId = userId
    )
} 