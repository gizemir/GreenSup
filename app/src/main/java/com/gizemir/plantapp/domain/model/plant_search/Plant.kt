package com.gizemir.plantapp.domain.model.plant_search

data class Plant(
    val id: Int,
    val commonName: String?,
    val scientificName: String,
    val imageUrl: String?,
    val family: String?,
    val genus: String?,
    val bibliography: String?,
    val author: String?,
    val status: String?,
    val rank: String?,
    val familyCommonName: String?
)

data class PlantDetail(
    val id: Int,
    val commonName: String?,
    val scientificName: String,
    val imageUrl: String?,
    val family: String?,
    val genus: String?,
    val author: String?,
    val bibliography: String?,
    val year: Int?,
    val status: String?,
    val rank: String?,
    val familyCommonName: String?,
    val genusId: Int?,
    val familyId: Int?,
    val synonyms: List<String>?,
    val hardinessMapUrl: String?,
    val distribution: PlantDistribution?,
    val careInfo: PlantCareInfo?
)

data class PlantDistribution(
    val native: List<String>?,
    val introduced: List<String>?,
    val doubtful: List<String>?,
    val absent: List<String>?,
    val extinct: List<String>?
)

data class PlantCareInfo(
    val sunlight: List<String>?,
    val watering: String?,
    val wateringDays: String?,
    val pruningMonths: List<String>?,
    val pruningFrequency: String?
)