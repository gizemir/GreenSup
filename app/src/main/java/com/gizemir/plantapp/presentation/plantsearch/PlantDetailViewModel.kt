package com.gizemir.plantapp.presentation.plantsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import com.gizemir.plantapp.domain.use_case.plant_search.GetPlantDetailUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import com.gizemir.plantapp.domain.model.garden.GardenPlant

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    private val getPlantDetailUseCase: GetPlantDetailUseCase,
    private val favoriteRepository: FavoriteRepository,
    private val gardenRepository: GardenRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<PlantDetailUiState>(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()
    
    fun loadPlantDetail(plantId: Int) {
        viewModelScope.launch {
            Log.d("PlantDetailViewModel", "🌱 Loading plant detail for ID: $plantId")
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val currentUser = FirebaseAuth.getInstance().currentUser
            val isFavorite = if (currentUser != null) {
                favoriteRepository.isFavorite(currentUser.uid, plantId)
            } else {
                false
            }
            Log.d("PlantDetailViewModel", "📋 Is favorite: $isFavorite for user: ${currentUser?.uid}")
            
            getPlantDetailUseCase(plantId).fold(
                onSuccess = { plantDetail ->
                    Log.d("PlantDetailViewModel", "✅ SUCCESS! Plant detail loaded successfully")
                    Log.d("PlantDetailViewModel", "📊 Plant ID: ${plantDetail.id}")
                    Log.d("PlantDetailViewModel", "📊 Common Name: ${plantDetail.commonName}")
                    Log.d("PlantDetailViewModel", "📊 Scientific Name: ${plantDetail.scientificName}")
                    Log.d("PlantDetailViewModel", "📊 Family: ${plantDetail.family}")
                    Log.d("PlantDetailViewModel", "📊 Hardiness Map URL: ${plantDetail.hardinessMapUrl}")
                    
                    // Distribution debug
                    Log.d("PlantDetailViewModel", "🌍 Distribution: ${plantDetail.distribution}")
                    if (plantDetail.distribution != null) {
                        Log.d("PlantDetailViewModel", "🌍 Native regions: ${plantDetail.distribution!!.native}")
                        Log.d("PlantDetailViewModel", "🌍 Introduced regions: ${plantDetail.distribution!!.introduced}")
                    } else {
                        Log.w("PlantDetailViewModel", "⚠️ Distribution is NULL - Geographic Distribution card will not show")
                    }
                    
                    // Care Info debug
                    Log.d("PlantDetailViewModel", "🌿 Care Info: ${plantDetail.careInfo}")
                    if (plantDetail.careInfo != null) {

                        Log.i("PlantDetailViewModel", "✅ Care Information card WILL show")
                    } else {
                        Log.w("PlantDetailViewModel", "⚠️ Care Info is NULL - Care Information card will not show")
                    }
                    

                    
                    _uiState.value = _uiState.value.copy(
                        plantDetail = plantDetail,
                        isLoading = false,
                        errorMessage = null,
                        isFavorite = isFavorite
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        plantDetail = null,
                        isLoading = false,
                        errorMessage = "An error occurred while loading plant details",
                        isFavorite = isFavorite
                    )
                }
            )
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val plantDetail = currentState.plantDetail ?: return@launch
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
            
            try {
                if (currentState.isFavorite) {
                    favoriteRepository.removeFromFavorites(currentUser.uid, plantDetail.id)
                    _uiState.value = currentState.copy(isFavorite = false)
                } else {
                    val favoritePlant = FavoritePlant(
                        userId = currentUser.uid,
                        plantId = plantDetail.id,
                        commonName = plantDetail.commonName ?: "Unknown Plant",
                        scientificName = plantDetail.scientificName,
                        imageUrl = plantDetail.imageUrl,
                        userImageUri = null,
                        family = plantDetail.family,
                        genus = plantDetail.genus,
                        addedAt = System.currentTimeMillis(),
                        source = com.gizemir.plantapp.domain.model.favorite.FavoriteSource.PLANT_SEARCH
                    )
                    favoriteRepository.addToFavorites(favoritePlant)
                    _uiState.value = currentState.copy(isFavorite = true)
                }
            } catch (e: Exception) {
                Log.e("PlantDetailViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun addPlantToGarden() {
        viewModelScope.launch {
            val plantDetail = _uiState.value.plantDetail ?: return@launch
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
            val wateringPeriod = plantDetail.careInfo?.wateringDays?.toIntOrNull() ?: 7
            val now = System.currentTimeMillis()
            val gardenPlant = GardenPlant(
                userId = currentUser.uid,
                plantId = plantDetail.id.toString(),
                name = plantDetail.commonName ?: plantDetail.scientificName,
                imageUrl = plantDetail.imageUrl,
                plantedDate = now,
                lastWateredDate = now,
                wateringPeriodDays = wateringPeriod
            )
            gardenRepository.addPlantToGarden(gardenPlant)
        }
    }
}

data class PlantDetailUiState(
    val plantDetail: PlantDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false
)
