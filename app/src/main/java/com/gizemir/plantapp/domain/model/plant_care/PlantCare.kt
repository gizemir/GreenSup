package com.gizemir.plantapp.domain.model.plant_care

data class PlantCare(
    val plantName: String,
    val scientificName: String,
    val watering: PlantCareSection,
    val lighting: PlantCareSection,
    val soil: PlantCareSection,
    val temperature: PlantCareSection,
    val humidity: PlantCareSection,
    val commonProblems: PlantCareSection,
    val generalTips: List<String>
)

data class PlantCareSection(
    val title: String,
    val description: String,
    val tips: List<String>
) 