package com.gizemir.plantapp.data.local.entity.garden

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.gizemir.plantapp.data.converter.GardenConverters
import com.gizemir.plantapp.domain.model.garden.GardenNote
import com.gizemir.plantapp.domain.model.garden.GardenPhoto

@Entity(tableName = "garden_plants")
data class GardenPlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val plantId: String,
    val name: String,
    val imageUrl: String?,
    val plantedDate: Long,
    val lastWateredDate: Long,
    val wateringPeriodDays: Int,
    val notes: List<GardenNote> = emptyList()
) 