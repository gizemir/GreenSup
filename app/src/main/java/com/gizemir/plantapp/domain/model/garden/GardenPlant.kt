package com.gizemir.plantapp.domain.model.garden

import java.util.Date



data class GardenPlant(
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

data class GardenNote(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Long,
    val note: String,
    val photoUrl: String? = null
)

data class GardenPhoto(
    val date: Long,
    val imageUrl: String
) 