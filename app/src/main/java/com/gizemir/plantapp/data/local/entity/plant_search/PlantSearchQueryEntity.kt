package com.gizemir.plantapp.data.local.entity.plant_search

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_search_queries")
data class PlantSearchQueryEntity(
    @PrimaryKey val query: String,
    val page: Int,
    val plantIds: List<Int>,
    val cached_at: Long = System.currentTimeMillis()
) 