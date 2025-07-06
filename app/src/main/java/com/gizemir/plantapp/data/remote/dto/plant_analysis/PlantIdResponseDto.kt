package com.gizemir.plantapp.data.remote.dto.plant_analysis

import com.google.gson.annotations.SerializedName

data class PlantIdResponseDto(
    @SerializedName("result")
    val result: ResultDto,
    @SerializedName("status")
    val status: String,
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("model_version")
    val modelVersion: String?,
    @SerializedName("custom_id")
    val customId: String?,
    @SerializedName("input")
    val input: InputDto?,
    @SerializedName("meta")
    val meta: MetaDto?
)

data class ResultDto(
    @SerializedName("is_plant")
    val isPlant: IsBooleanDto,
    @SerializedName("is_healthy")
    val isHealthy: IsBooleanDto?,
    @SerializedName("disease")
    val disease: DiseaseDto?,
    @SerializedName("classification")
    val classification: ClassificationDto?
)

data class IsBooleanDto(
    @SerializedName("probability")
    val probability: Double,
    @SerializedName("binary")
    val binary: Boolean,
    @SerializedName("threshold")
    val threshold: Double?
)

data class DiseaseDto(
    @SerializedName("suggestions")
    val suggestions: List<DiseaseSuggestionDto>?
)

data class DiseaseSuggestionDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("probability")
    val probability: Double,
    @SerializedName("similar_images")
    val similarImages: List<SimilarImageDto>?,
    @SerializedName("details")
    val details: DiseaseDetailsDto?
)

data class DiseaseDetailsDto(
    @SerializedName("common_names")
    val commonNames: List<String>?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("treatment")
    val treatment: TreatmentDto?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("cause")
    val cause: String?
)

data class TreatmentDto(
    @SerializedName("biological")
    val biological: List<String>?,
    @SerializedName("chemical")
    val chemical: List<String>?,
    @SerializedName("prevention")
    val prevention: List<String>?
)

data class ClassificationDto(
    @SerializedName("suggestions")
    val suggestions: Any?
)

data class PlantSuggestionDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("probability")
    val probability: Double,
    @SerializedName("similar_images")
    val similarImages: List<SimilarImageDto>?,
    @SerializedName("details")
    val details: PlantDetailsDto?
)

data class PlantDetailsDto(
    @SerializedName("common_names")
    val commonNames: List<String>?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("name_authority")
    val nameAuthority: String?,
    @SerializedName("wiki_description")
    val wikiDescription: WikiDescriptionDto?,
    @SerializedName("taxonomy")
    val taxonomy: TaxonomyDto?,
    @SerializedName("synonyms")
    val synonyms: List<String>?,
    @SerializedName("edible_parts")
    val edibleParts: List<String>?,
    @SerializedName("watering")
    val watering: WateringDto?,
    @SerializedName("propagation_methods")
    val propagationMethods: List<String>?
)

data class WikiDescriptionDto(
    @SerializedName("value")
    val value: String?,
    @SerializedName("citation")
    val citation: String?,
    @SerializedName("license_name")
    val licenseName: String?,
    @SerializedName("license_url")
    val licenseUrl: String?
)

data class TaxonomyDto(
    @SerializedName("kingdom")
    val kingdom: String?,
    @SerializedName("phylum")
    val phylum: String?,
    @SerializedName("class")
    val className: String?,
    @SerializedName("order")
    val order: String?,
    @SerializedName("family")
    val family: String?,
    @SerializedName("genus")
    val genus: String?
)

data class WateringDto(
    @SerializedName("max")
    val max: Int?,
    @SerializedName("min")
    val min: Int?
)

data class SimilarImageDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("similarity")
    val similarity: Double?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("url_small")
    val urlSmall: String?
)

data class InputDto(
    @SerializedName("images")
    val images: List<String>?,
    @SerializedName("health")
    val health: String?,
    @SerializedName("classification_level")
    val classificationLevel: String?,
    @SerializedName("similar_images")
    val similarImages: Boolean?,
    @SerializedName("symptoms")
    val symptoms: Boolean?
)

data class MetaDto(
    @SerializedName("identification")
    val identification: String?
) 