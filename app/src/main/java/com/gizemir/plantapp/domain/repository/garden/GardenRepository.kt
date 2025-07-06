package com.gizemir.plantapp.domain.repository.garden

import com.gizemir.plantapp.domain.model.garden.GardenNote
import com.gizemir.plantapp.domain.model.garden.GardenPhoto
import com.gizemir.plantapp.domain.model.garden.GardenPlant
import kotlinx.coroutines.flow.Flow

interface GardenRepository {
    fun getGardenPlants(userId: String): Flow<List<GardenPlant>>
    fun getPlantById(plantId: Long): Flow<GardenPlant?>
    suspend fun addPlantToGarden(plant: GardenPlant)
    suspend fun removePlantFromGarden(plantId: String)
    suspend fun updatePlant(plant: GardenPlant)
    suspend fun addNoteToPlant(plantId: Long, note: GardenNote)
    suspend fun updateNoteInPlant(plantId: Long, noteId: String, updatedNote: String, updatedPhotoUrl: String?)
    suspend fun deleteNoteFromPlant(plantId: Long, noteId: String)
    suspend fun clearGardenData()
} 