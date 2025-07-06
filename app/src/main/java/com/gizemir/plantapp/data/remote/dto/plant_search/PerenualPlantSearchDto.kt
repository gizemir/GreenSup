package com.gizemir.plantapp.data.remote.dto.plant_search

import com.google.gson.annotations.SerializedName
import com.gizemir.plantapp.domain.model.plant_search.Plant

data class PerenualPlantSearchResponseDto(
    @SerializedName("data") val data: List<PerenualPlantDto>,
    @SerializedName("to") val to: Int?,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("current_page") val currentPage: Int?,
    @SerializedName("from") val from: Int?,
    @SerializedName("last_page") val lastPage: Int?,
    @SerializedName("total") val total: Int?
)

data class PerenualPlantDto(
    @SerializedName("id") val id: Int,
    @SerializedName("common_name") val commonName: String?,
    @SerializedName("scientific_name") val scientificName: List<String>?,
    @SerializedName("other_name") val otherName: List<String>?,
    @SerializedName("family") val family: String?,
    @SerializedName("genus") val genus: String?,
    @SerializedName("species_epithet") val speciesEpithet: String?,
    @SerializedName("default_image") val defaultImage: PerenualImageDto?
) {
    fun toPlant(): Plant {
        return Plant(
            id = id,
            commonName = commonName,
            scientificName = scientificName?.firstOrNull() ?: "Unknown",
            imageUrl = defaultImage?.regularUrl ?: defaultImage?.originalUrl,
            family = family,
            genus = genus,
            bibliography = "",
            author = "",
            status = "accepted",
            rank = "species",
            familyCommonName = family
        )
    }
}

 