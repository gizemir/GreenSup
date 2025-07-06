package com.gizemir.plantapp.presentation.garden

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.core.util.ImageUtils
import com.gizemir.plantapp.core.worker.WateringSchedulerService
import com.gizemir.plantapp.domain.model.garden.GardenPlant
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val gardenRepository: GardenRepository,
    private val wateringSchedulerService: WateringSchedulerService,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _gardenPlants = MutableStateFlow<List<GardenPlant>>(emptyList())
    val gardenPlants: StateFlow<List<GardenPlant>> = _gardenPlants.asStateFlow()

    private val _selectedPlant = MutableStateFlow<GardenPlant?>(null)
    val selectedPlant: StateFlow<GardenPlant?> = _selectedPlant

    init {
        loadGardenPlants()
    }

    private fun loadGardenPlants() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.id?.let { userId ->
                gardenRepository.getGardenPlants(userId).collect { plants ->
                    _gardenPlants.value = plants
                }
            }
        }
    }

    fun waterPlant(plant: GardenPlant) = viewModelScope.launch {
        try {
            val updatedPlant = plant.copy(lastWateredDate = System.currentTimeMillis())
            gardenRepository.updatePlant(updatedPlant)
            
            wateringSchedulerService.scheduleWateringReminder(updatedPlant, updatedPlant.userId)
            Log.d("GardenViewModel", "Scheduled watering reminder for ${plant.name} in ${plant.wateringPeriodDays} days")
        } catch (e: Exception) {
            Log.e("GardenViewModel", "Error watering plant ${plant.name}", e)
        }
    }

    fun addNoteToPlant(plantId: Long, noteText: String, photoUrl: String? = null) {
        viewModelScope.launch {
            try {
                val persistentPhotoUrl = photoUrl?.let { url ->
                    if (url.startsWith("content://")) {
                        ImageUtils.copyUriToInternalStorage(
                            context,
                            Uri.parse(url),
                            "garden_note"
                        )
                    } else {
                        url
                    }
                }
                
                val newNote = com.gizemir.plantapp.domain.model.garden.GardenNote(
                    note = noteText, 
                    date = System.currentTimeMillis(),
                    photoUrl = persistentPhotoUrl
                )
                gardenRepository.addNoteToPlant(plantId, newNote)
                Log.d("GardenViewModel", "Note added with photo: $persistentPhotoUrl")
            } catch (e: Exception) {
                Log.e("GardenViewModel", "Error adding note to plant", e)
            }
        }
    }

    fun updateNoteInPlant(plantId: Long, noteId: String, updatedNote: String, updatedPhotoUrl: String?) {
        viewModelScope.launch {
            try {
                val persistentPhotoUrl = updatedPhotoUrl?.let { url ->
                    if (url.startsWith("content://")) {
                        ImageUtils.copyUriToInternalStorage(
                            context,
                            Uri.parse(url),
                            "garden_note"
                        )
                    } else {
                        url
                    }
                }
                
                gardenRepository.updateNoteInPlant(plantId, noteId, updatedNote, persistentPhotoUrl)
                Log.d("GardenViewModel", "Note updated with photo: $persistentPhotoUrl")
            } catch (e: Exception) {
                Log.e("GardenViewModel", "Error updating note in plant", e)
            }
        }
    }

    fun deleteNoteFromPlant(plantId: Long, noteId: String) {
        viewModelScope.launch {
            gardenRepository.deleteNoteFromPlant(plantId, noteId)
        }
    }

    fun addPlantToGarden(plant: GardenPlant) = viewModelScope.launch {
        try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                Log.e("GardenViewModel", "Cannot add plant to garden, user not logged in")
                return@launch
            }
            
            val plantWithOwner = plant.copy(userId = currentUser.id)
            gardenRepository.addPlantToGarden(plantWithOwner)
            
            if (plantWithOwner.lastWateredDate > 0) {
                wateringSchedulerService.scheduleWateringReminder(plantWithOwner, plantWithOwner.userId)
                Log.d("GardenViewModel", "Scheduled watering reminder for new plant ${plant.name}")
            }
        } catch (e: Exception) {
            Log.e("GardenViewModel", "Error adding plant to garden ${plant.name}", e)
        }
    }

    fun removePlantFromGarden(plantId: String) = viewModelScope.launch {
        try {
            val plant = _gardenPlants.value.find { it.plantId == plantId }
            plant?.let {
                wateringSchedulerService.cancelWateringReminder(it.id)
                Log.d("GardenViewModel", "Cancelled watering reminder for removed plant ${it.name}")
            }
            
            gardenRepository.removePlantFromGarden(plantId)
        } catch (e: Exception) {
            Log.e("GardenViewModel", "Error removing plant from garden $plantId", e)
        }
    }

    fun isPlantInGarden(plantId: String): Boolean {
        return _gardenPlants.value.any { it.plantId == plantId }
    }

    fun loadPlantById(plantId: Long) {
        viewModelScope.launch {
            gardenRepository.getPlantById(plantId).collect { plant ->
                _selectedPlant.value = plant
            }
        }
    }

    fun updateLastWateredDate(plantId: Long, newDate: Long) = viewModelScope.launch {
        selectedPlant.value?.let {
            val updatedPlant = it.copy(lastWateredDate = newDate)
            gardenRepository.updatePlant(updatedPlant)
        }
    }
} 