package com.gizemir.plantapp.data.local.dao.garden

import androidx.room.*
import com.gizemir.plantapp.data.local.entity.garden.GardenPlantEntity
import com.gizemir.plantapp.domain.model.garden.GardenNote
import com.gizemir.plantapp.domain.model.garden.GardenPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface GardenPlantDao {
    @Query("SELECT * FROM garden_plants WHERE userId = :userId")
    fun getAllPlantsForUser(userId: String): Flow<List<GardenPlantEntity>>

    @Query("SELECT * FROM garden_plants WHERE id = :plantId")
    fun getPlantById(plantId: Long): Flow<GardenPlantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plant: GardenPlantEntity)

    @Update
    suspend fun update(plant: GardenPlantEntity)

    @Query("DELETE FROM garden_plants WHERE plantId = :plantId")
    suspend fun deletePlantByPlantId(plantId: String)

    @Query("UPDATE garden_plants SET notes = :notes WHERE id = :plantId")
    suspend fun updateNotes(plantId: Long, notes: List<GardenNote>)

    @Query("DELETE FROM garden_plants")
    suspend fun clearAll()
} 