package com.gizemir.plantapp.data.remote.dto.plant_search

import com.google.gson.annotations.SerializedName
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail


data class PerenualPlantDetailResponseDto(
    @SerializedName("id") val id: Int,
    @SerializedName("common_name") val commonName: String?,
    @SerializedName("scientific_name") val scientificName: List<String>?,
    @SerializedName("other_name") val otherName: List<String>?,
    @SerializedName("family") val family: String?,
    @SerializedName("origin") val origin: List<String>?,
    @SerializedName("sunlight") val sunlight: List<String>?,
    @SerializedName("watering") val watering: String?,
    @SerializedName("watering_general_benchmark") val wateringBenchmark: PerenualWateringBenchmarkDto?,
    @SerializedName("pruning_month") val pruningMonth: List<String>?,
    @SerializedName("hardiness_location") val hardinessLocation: PerenualHardinessLocationDto?,
    @SerializedName("default_image") val defaultImage: PerenualImageDto?
) {
    fun toPlantDetail(): PlantDetail {
        return PlantDetail(
            id = id,
            commonName = commonName,
            scientificName = scientificName?.firstOrNull() ?: "Unknown",
            imageUrl = defaultImage?.regularUrl ?: defaultImage?.originalUrl,
            family = family,
            genus = scientificName?.firstOrNull()?.split(" ")?.firstOrNull(),
            author = null,
            bibliography = null,
            year = null,
            status = "accepted",
            rank = "species",
            familyCommonName = family,
            genusId = null,
            familyId = null,
            synonyms = otherName,
            hardinessMapUrl = hardinessLocation?.fullUrl,
            distribution = com.gizemir.plantapp.domain.model.plant_search.PlantDistribution(
                native = origin,
                introduced = null,
                doubtful = null,
                absent = null,
                extinct = null
            ),
            careInfo = com.gizemir.plantapp.domain.model.plant_search.PlantCareInfo(
                sunlight = sunlight,
                watering = watering,
                wateringDays = wateringBenchmark?.value,
                pruningMonths = pruningMonth,
                pruningFrequency = if (!pruningMonth.isNullOrEmpty()) "1-2 times yearly" else null
            )
        )
    }
}

data class PerenualWateringBenchmarkDto(
    @SerializedName("value") val value: String?,
    @SerializedName("unit") val unit: String?
)



data class PerenualHardinessLocationDto(
    @SerializedName("full_url") val fullUrl: String?,
    @SerializedName("full_iframe") val fullIframe: String?
)

data class PerenualImageDto(
    @SerializedName("original_url") val originalUrl: String?,
    @SerializedName("regular_url") val regularUrl: String?,
    @SerializedName("medium_url") val mediumUrl: String?,
    @SerializedName("small_url") val smallUrl: String?,
    @SerializedName("thumbnail") val thumbnail: String?
) 