package com.gizemir.plantapp.data.remote.dto.plant_analysis

import com.google.gson.annotations.SerializedName

data class PlantIdRequestDto(
    @SerializedName("images")
    val images: List<String>,
    @SerializedName("health")
    val health: String = "all",
    @SerializedName("classification_level")
    val classificationLevel: String = "species",
    @SerializedName("classification_raw")
    val classificationRaw: Boolean = true,
    @SerializedName("symptoms")
    val symptoms: Boolean? = null,
    @SerializedName("similar_images")
    val similarImages: Boolean = true
) 