package com.gizemir.plantapp.domain.model.plant_analysis

data class DiseaseDetection(
    val id: String,
    val imageUri: String,
    val isPlant: Boolean,
    val isHealthy: Boolean,
    val plantProbability: Double,
    val healthyProbability: Double,
    val diseases: List<Disease>,
    val plantSuggestions: List<PlantSuggestion>,
    val analyzedAt: Long = System.currentTimeMillis()
)

data class Disease(
    val id: String,
    val name: String,
    val probability: Double,
    val commonNames: List<String>,
    val description: String?,
    val treatment: Treatment?,
    val url: String?,
    val cause: String?,
    val similarImages: List<SimilarImage>
)

data class Treatment(
    val biological: List<String>,
    val chemical: List<String>,
    val prevention: List<String>
)

data class PlantSuggestion(
    val id: String,
    val name: String,
    val probability: Double,
    val commonNames: List<String>,
    val url: String?,
    val nameAuthority: String?,
    val wikiDescription: WikiDescription?,
    val taxonomy: Taxonomy?,
    val synonyms: List<String>,
    val edibleParts: List<String>,
    val watering: Watering?,
    val propagationMethods: List<String>,
    val similarImages: List<SimilarImage> = emptyList()
)

data class WikiDescription(
    val value: String,
    val citation: String?,
    val licenseName: String?,
    val licenseUrl: String?
)

data class Taxonomy(
    val kingdom: String?,
    val phylum: String?,
    val className: String?,
    val order: String?,
    val family: String?,
    val genus: String?
)

data class Watering(
    val max: Int?,
    val min: Int?
)

data class SimilarImage(
    val id: String,
    val similarity: Double,
    val url: String,
    val urlSmall: String?
) 