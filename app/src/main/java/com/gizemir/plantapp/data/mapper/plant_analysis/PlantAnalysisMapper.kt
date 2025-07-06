package com.gizemir.plantapp.data.mapper.plant_analysis

import com.gizemir.plantapp.data.remote.dto.plant_analysis.*
import com.gizemir.plantapp.domain.model.plant_analysis.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

fun PlantIdResponseDto.toDomainModel(imageUri: String): DiseaseDetection {
    return DiseaseDetection(
        id = meta?.identification ?: UUID.randomUUID().toString(),
        imageUri = imageUri,
        isPlant = result.isPlant.binary,
        isHealthy = result.isHealthy?.binary ?: false,
        plantProbability = result.isPlant.probability,
        healthyProbability = result.isHealthy?.probability ?: 0.0,
        diseases = result.disease?.suggestions?.map { it.toDomainModel() } ?: emptyList(),
        plantSuggestions = parseClassificationSuggestions(result.classification),
        analyzedAt = System.currentTimeMillis()
    )
}

private fun parseClassificationSuggestions(classification: ClassificationDto?): List<PlantSuggestion> {
    return try {
        val suggestions = classification?.suggestions
        android.util.Log.d("DiseaseMapper", "Raw suggestions: $suggestions")
        android.util.Log.d("DiseaseMapper", "Suggestions type: ${suggestions?.javaClass?.simpleName}")
        
        when (suggestions) {
            is Map<*, *> -> {
                android.util.Log.d("DiseaseMapper", "Processing as Map")
                val gson = Gson()
                
                // Species dizisini kontrol et
                val speciesList = suggestions["species"] as? List<*>
                if (!speciesList.isNullOrEmpty()) {
                    android.util.Log.d("DiseaseMapper", "Found species list with ${speciesList.size} items")
                    val listType = object : TypeToken<List<PlantSuggestionDto>>() {}.type
                    val suggestionsList: List<PlantSuggestionDto> = gson.fromJson(gson.toJson(speciesList), listType)
                    val result = suggestionsList.map { it.toDomainModel() }
                    android.util.Log.d("DiseaseMapper", "Mapped species result count: ${result.size}")
                    return result
                }

                val genusList = suggestions["genus"] as? List<*>
                if (!genusList.isNullOrEmpty()) {
                    android.util.Log.d("DiseaseMapper", "Found genus list with ${genusList.size} items")
                    val listType = object : TypeToken<List<PlantSuggestionDto>>() {}.type
                    val suggestionsList: List<PlantSuggestionDto> = gson.fromJson(gson.toJson(genusList), listType)
                    val result = suggestionsList.map { it.toDomainModel() }
                    android.util.Log.d("DiseaseMapper", "Mapped genus result count: ${result.size}")
                    return result
                }
                
                android.util.Log.d("DiseaseMapper", "No species or genus found in map")
                emptyList()
            }
            is List<*> -> {
                android.util.Log.d("DiseaseMapper", "Processing as List, size: ${suggestions.size}")
                val gson = Gson()
                val listType = object : TypeToken<List<PlantSuggestionDto>>() {}.type
                val suggestionsList: List<PlantSuggestionDto> = gson.fromJson(gson.toJson(suggestions), listType)
                val result = suggestionsList.map { it.toDomainModel() }
                android.util.Log.d("DiseaseMapper", "Mapped list result count: ${result.size}")
                result
            }
            else -> {
                android.util.Log.d("DiseaseMapper", "No suggestions or unrecognized type")
                emptyList()
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("DiseaseMapper", "Error parsing classification suggestions", e)
        emptyList()
    }
}

fun DiseaseSuggestionDto.toDomainModel(): Disease {
    return Disease(
        id = id ?: UUID.randomUUID().toString(),
        name = name ?: "Unknown Disease",
        probability = probability,
        commonNames = details?.commonNames ?: emptyList(),
        description = details?.description,
        treatment = details?.treatment?.toDomainModel(),
        url = details?.url,
        cause = details?.cause,
        similarImages = similarImages?.map { it.toDomainModel() } ?: emptyList()
    )
}

fun TreatmentDto.toDomainModel(): Treatment {
    return Treatment(
        biological = biological ?: emptyList(),
        chemical = chemical ?: emptyList(),
        prevention = prevention ?: emptyList()
    )
}

fun PlantSuggestionDto.toDomainModel(): PlantSuggestion {
    android.util.Log.d("DiseaseMapper", "Mapping PlantSuggestion: id=$id, name=$name, probability=$probability")
    android.util.Log.d("DiseaseMapper", "Details available: ${details != null}")
    android.util.Log.d("DiseaseMapper", "Common names: ${details?.commonNames}")
    
    val displayName = when {
        !details?.commonNames.isNullOrEmpty() -> details!!.commonNames!!.first()
        !name.isNullOrBlank() -> name!!
        !details?.nameAuthority.isNullOrBlank() -> details!!.nameAuthority!!
        else -> "Unknown Plant"
    }
    
    android.util.Log.d("DiseaseMapper", "Final display name: $displayName")
    
    return PlantSuggestion(
        id = id ?: UUID.randomUUID().toString(),
        name = displayName,
        probability = probability,
        commonNames = details?.commonNames ?: emptyList(),
        url = details?.url,
        nameAuthority = details?.nameAuthority,
        wikiDescription = details?.wikiDescription?.toDomainModel(),
        taxonomy = details?.taxonomy?.toDomainModel(),
        synonyms = details?.synonyms ?: emptyList(),
        edibleParts = details?.edibleParts ?: emptyList(),
        watering = details?.watering?.toDomainModel(),
        propagationMethods = details?.propagationMethods ?: emptyList(),
        similarImages = similarImages?.map { it.toDomainModel() } ?: emptyList()
    )
}

fun WikiDescriptionDto.toDomainModel(): WikiDescription {
    return WikiDescription(
        value = value ?: "",
        citation = citation,
        licenseName = licenseName,
        licenseUrl = licenseUrl
    )
}

fun TaxonomyDto.toDomainModel(): Taxonomy {
    return Taxonomy(
        kingdom = kingdom,
        phylum = phylum,
        className = className,
        order = order,
        family = family,
        genus = genus
    )
}

fun WateringDto.toDomainModel(): Watering {
    return Watering(
        max = max,
        min = min
    )
}

fun SimilarImageDto.toDomainModel(): SimilarImage {
    return SimilarImage(
        id = id ?: UUID.randomUUID().toString(),
        similarity = similarity ?: 0.0,
        url = url ?: "",
        urlSmall = urlSmall
    )
} 