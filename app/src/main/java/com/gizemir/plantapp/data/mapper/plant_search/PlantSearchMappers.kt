package com.gizemir.plantapp.data.mapper.plant_search

import com.gizemir.plantapp.domain.model.plant_search.Plant
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail

fun com.gizemir.plantapp.data.local.entity.plant_search.PlantEntity.toPlant(): Plant {
    return Plant(
        id = id,
        commonName = commonName,
        scientificName = scientificName,
        imageUrl = imageUrl,
        family = family,
        genus = genus,
        bibliography = bibliography,
        author = author,
        status = status,
        rank = rank,
        familyCommonName = familyCommonName
    )
}

fun com.gizemir.plantapp.data.local.entity.plant_search.PlantDetailEntity.toPlant(): PlantDetail {
    return PlantDetail(
        id = id,
        commonName = commonName,
        scientificName = scientificName,
        imageUrl = imageUrl,
        family = family,
        genus = genus,
        author = author,
        bibliography = bibliography,
        year = year,
        status = status,
        rank = rank,
        familyCommonName = familyCommonName,
        genusId = genusId,
        familyId = familyId,
        synonyms = synonyms,
        hardinessMapUrl = hardinessMapUrl,
        distribution = null,
        careInfo = null
    )
} 