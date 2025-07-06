package com.gizemir.plantapp.data.local.entity.plant_search

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gizemir.plantapp.domain.model.plant_search.Plant

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey val id: Int,
    val commonName: String?,
    val scientificName: String,
    val imageUrl: String?,
    val family: String?,
    val genus: String?,
    val bibliography: String?,
    val author: String?,
    val status: String?,
    val rank: String?,
    val familyCommonName: String?,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toPlant(): Plant {
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
    
    companion object {
        fun fromPlant(plant: Plant): PlantEntity {
            return PlantEntity(
                id = plant.id,
                commonName = plant.commonName,
                scientificName = plant.scientificName,
                imageUrl = plant.imageUrl,
                family = plant.family,
                genus = plant.genus,
                bibliography = plant.bibliography,
                author = plant.author,
                status = plant.status,
                rank = plant.rank,
                familyCommonName = plant.familyCommonName
            )
        }
    }
} 