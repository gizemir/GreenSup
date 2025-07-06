package com.gizemir.plantapp.data.local.entity.plant_search

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail
import com.gizemir.plantapp.domain.model.plant_search.PlantDistribution
import com.gizemir.plantapp.domain.model.plant_search.PlantCareInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "plant_details")
data class PlantDetailEntity(
    @PrimaryKey val id: Int,
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
    val distributionJson: String?,
    val careInfoJson: String?,
    val cached_at: Long = System.currentTimeMillis()
) {
    fun toPlantDetail(): PlantDetail {
        val gson = Gson()
        
        val distribution = distributionJson?.let {
            try {
                gson.fromJson(it, PlantDistribution::class.java)
            } catch (e: Exception) {
                null
            }
        }

        val careInfo = careInfoJson?.let {
            try {
                gson.fromJson(it, PlantCareInfo::class.java)
            } catch (e: Exception) {
                null
            }
        }
        
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
            distribution = distribution,
            careInfo = careInfo
        )
    }
    
    companion object {
        fun fromPlantDetail(plantDetail: PlantDetail): PlantDetailEntity {
            val gson = Gson()

            val distributionJson = plantDetail.distribution?.let {
                try {
                    gson.toJson(it)
                } catch (e: Exception) {
                    null
                }
            }
            
            val careInfoJson = plantDetail.careInfo?.let {
                try {
                    gson.toJson(it)
                } catch (e: Exception) {
                    null
                }
            }
            
            return PlantDetailEntity(
                id = plantDetail.id,
                commonName = plantDetail.commonName,
                scientificName = plantDetail.scientificName,
                imageUrl = plantDetail.imageUrl,
                family = plantDetail.family,
                genus = plantDetail.genus,
                author = plantDetail.author,
                bibliography = plantDetail.bibliography,
                year = plantDetail.year,
                status = plantDetail.status,
                rank = plantDetail.rank,
                familyCommonName = plantDetail.familyCommonName,
                genusId = plantDetail.genusId,
                familyId = plantDetail.familyId,
                synonyms = plantDetail.synonyms,
                hardinessMapUrl = plantDetail.hardinessMapUrl,
                distributionJson = distributionJson,
                careInfoJson = careInfoJson
            )
        }
    }
} 