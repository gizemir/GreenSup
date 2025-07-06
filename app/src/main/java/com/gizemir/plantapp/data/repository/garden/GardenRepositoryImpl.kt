package com.gizemir.plantapp.data.repository.garden

import com.gizemir.plantapp.data.local.dao.garden.GardenPlantDao
import com.gizemir.plantapp.data.mapper.garden.toGardenPlant
import com.gizemir.plantapp.data.mapper.garden.toGardenPlantEntity
import com.gizemir.plantapp.domain.model.garden.GardenNote
import com.gizemir.plantapp.domain.model.garden.GardenPhoto
import com.gizemir.plantapp.domain.model.garden.GardenPlant
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GardenRepositoryImpl @Inject constructor(
    private val gardenPlantDao: GardenPlantDao
) : GardenRepository {
    override fun getGardenPlants(userId: String): Flow<List<GardenPlant>> {
        return gardenPlantDao.getAllPlantsForUser(userId).map { entities ->
            entities.map { it.toGardenPlant() }
        }
    }

    override fun getPlantById(plantId: Long): Flow<GardenPlant?> {
        return gardenPlantDao.getPlantById(plantId).map { it?.toGardenPlant() }
    }

    override suspend fun addPlantToGarden(plant: GardenPlant) {
        gardenPlantDao.insert(plant.toGardenPlantEntity())
    }

    override suspend fun removePlantFromGarden(plantId: String) {
        gardenPlantDao.deletePlantByPlantId(plantId)
    }

    override suspend fun updatePlant(plant: GardenPlant) {
        gardenPlantDao.update(plant.toGardenPlantEntity())
    }

    override suspend fun addNoteToPlant(plantId: Long, note: GardenNote) {
        val plantEntity = gardenPlantDao.getPlantById(plantId).first()
        plantEntity?.let {
            val updatedNotes = it.notes + note
            gardenPlantDao.updateNotes(plantId, updatedNotes)
        }
    }

    override suspend fun updateNoteInPlant(plantId: Long, noteId: String, updatedNote: String, updatedPhotoUrl: String?) {
        val plantEntity = gardenPlantDao.getPlantById(plantId).first()
        plantEntity?.let {
            val updatedNotes = it.notes.map { note ->
                if (note.id == noteId) {
                    note.copy(note = updatedNote, photoUrl = updatedPhotoUrl)
                } else {
                    note
                }
            }
            gardenPlantDao.updateNotes(plantId, updatedNotes)
        }
    }

    override suspend fun deleteNoteFromPlant(plantId: Long, noteId: String) {
        val plantEntity = gardenPlantDao.getPlantById(plantId).first()
        plantEntity?.let {
            val updatedNotes = it.notes.filter { note -> note.id != noteId }
            gardenPlantDao.updateNotes(plantId, updatedNotes)
        }
    }

    override suspend fun clearGardenData() {
        gardenPlantDao.clearAll()
    }
} 