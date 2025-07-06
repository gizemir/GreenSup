package com.gizemir.plantapp.presentation.plant_analysis

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import com.gizemir.plantapp.domain.model.favorite.FavoriteSource
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import com.gizemir.plantapp.domain.use_case.plant_analysis.PlantAnalysisUseCase
import com.gizemir.plantapp.domain.use_case.plant_analysis.SavePlantAnalysisUseCase
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import com.gizemir.plantapp.domain.util.Resource
import com.gizemir.plantapp.presentation.plant_analysis.state.PlantAnalysisState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import com.gizemir.plantapp.domain.model.garden.GardenPlant

@HiltViewModel
class PlantAnalysisViewModel @Inject constructor(
    private val plantAnalysisUseCase: PlantAnalysisUseCase,
    private val savePlantAnalysisUseCase: SavePlantAnalysisUseCase,
    private val plantAnalysisRepository: PlantAnalysisRepository,
    private val favoriteRepository: FavoriteRepository,
    private val gardenRepository: GardenRepository
) : ViewModel() {

    private val _uiState = mutableStateOf(PlantAnalysisState())
    val uiState: State<PlantAnalysisState> = _uiState

    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            errorMessage = null,
            showResults = false,
            diseaseDetection = null
        )
        Log.d("DetectDiseaseViewModel", "Image selected: $uri")
    }

    fun clearSelectedImage() {
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            errorMessage = null,
            showResults = false,
            diseaseDetection = null
        )
        Log.d("DetectDiseaseViewModel", "Image cleared")
    }

    fun identifyPlant() {
        val currentState = _uiState.value
        val imageUri = currentState.selectedImageUri

        if (imageUri == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Lütfen bir fotoğraf seçin"
            )
            return
        }

        Log.d("DetectDiseaseViewModel", "Starting smart plant analysis for image: $imageUri")

        viewModelScope.launch {
            _uiState.value = currentState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val result = plantAnalysisUseCase.identifyPlantOnly(imageUri)
                
                when (result) {
                    is Resource.Success -> {
                        Log.d("DetectDiseaseViewModel", "Plant analysis successful")
                        val detection = result.data!!
                        
                        saveDiseaseDetection(detection)
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            diseaseDetection = detection,
                            showResults = true,
                            errorMessage = null
                        )
                    }
                    
                    is Resource.Error -> {
                        Log.e("DetectDiseaseViewModel", "Plant analysis failed: ${result.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message ?: "Plant analysis failed",
                            showResults = false
                        )
                    }
                    
                    is Resource.Loading -> {
                    }
                }
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Exception during plant analysis", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Beklenmeyen hata: ${e.localizedMessage}",
                    showResults = false
                )
            }
        }
    }

    private suspend fun saveDiseaseDetection(detection: com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val userId = currentUser?.uid
            
            savePlantAnalysisUseCase(detection, userId)
            Log.d("DetectDiseaseViewModel", "Disease detection saved to database")
        } catch (e: Exception) {
            Log.e("DetectDiseaseViewModel", "Error saving disease detection", e)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            showResults = false,
            diseaseDetection = null,
            errorMessage = null
        )
    }

    suspend fun checkIfFavorite(plantId: String): Boolean {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return false
            favoriteRepository.isFavorite(currentUser.uid, plantId.hashCode())
        } catch (e: Exception) {
            false
        }
    }

    fun addToFavorites(plantId: String, plantName: String, scientificName: String, imageUrl: String?) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                val isDiseaseRelated = isPlantDiseaseRelated(plantName, scientificName)
                
                val favoritePlant = FavoritePlant(
                    userId = currentUser.uid,
                    plantId = plantId.hashCode(),
                    commonName = plantName,
                    scientificName = scientificName,
                    imageUrl = imageUrl,
                    userImageUri = _uiState.value.selectedImageUri?.toString(),
                    family = if (isDiseaseRelated) "Pathogen" else null,
                    genus = null,
                    addedAt = System.currentTimeMillis(),
                    source = if (isDiseaseRelated) FavoriteSource.DISEASE_ANALYSIS else FavoriteSource.PLANT_ID_DETAIL
                )
                favoriteRepository.addToFavorites(favoritePlant)
                
                val logMessage = if (isDiseaseRelated) "Disease-related organism" else "Plant"
                Log.d("DetectDiseaseViewModel", "$logMessage added to favorites: $plantName")
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Error adding to favorites", e)
            }
        }
    }
    
    private fun isPlantDiseaseRelated(plantName: String, scientificName: String): Boolean {
        val diseaseKeywords = listOf(
            "fungi", "fungus", "pathogen", "virus", "bacteria", "bacterium",
            "disease", "blight", "rot", "mold", "mould", "rust", "spot",
            "wilt", "canker", "scab", "powdery", "downy", "anthracnose",
            "fusarium", "phytophthora", "botrytis", "alternaria", "cercospora",
            "septoria", "verticillium", "rhizoctonia", "pythium", "sclerotinia",
            "colletotrichum", "xanthomonas", "pseudomonas", "erwinia",
            "agrobacterium", "ralstonia"
        )
        
        val searchText = "$plantName $scientificName".lowercase()
        return diseaseKeywords.any { keyword -> searchText.contains(keyword) }
    }

    fun addDiseaseToFavorites(diseaseId: String, diseaseName: String, scientificName: String, imageUrl: String?) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                val favoritePlant = FavoritePlant(
                    userId = currentUser.uid,
                    plantId = diseaseId.hashCode(),
                    commonName = diseaseName,
                    scientificName = scientificName,
                    imageUrl = imageUrl,
                    userImageUri = _uiState.value.selectedImageUri?.toString(),
                    family = null, // Disease için family bilgisi "Disease Type" gibi bir şey olabilir
                    genus = null,
                    addedAt = System.currentTimeMillis(),
                    source = FavoriteSource.DISEASE_ANALYSIS
                )
                favoriteRepository.addToFavorites(favoritePlant)
                Log.d("DetectDiseaseViewModel", "Disease added to favorites: $diseaseName")
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Error adding disease to favorites", e)
            }
        }
    }

    fun removeFromFavorites(plantId: String) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                favoriteRepository.removeFromFavorites(currentUser.uid, plantId.hashCode())
                Log.d("DetectDiseaseViewModel", "Plant removed from favorites")
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Error removing from favorites", e)
            }
        }
    }

    fun loadPlantFromHistory(plantId: String, callback: (com.gizemir.plantapp.domain.model.plant_analysis.PlantSuggestion?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("DetectDiseaseViewModel", "Loading plant from history: $plantId")
                
                val foundPlant = _uiState.value.diseaseDetection?.plantSuggestions?.find { it.id == plantId }
                if (foundPlant != null) {
                    Log.d("DetectDiseaseViewModel", "Plant found in current detection: ${foundPlant.name}")
                    callback(foundPlant)
                    return@launch
                }
                
                val detections = plantAnalysisRepository.getAllDiseaseDetections()
                detections.collect { detectionList ->
                    for (detection in detectionList) {
                        val matchingPlant = detection.plantSuggestions.find { it.id == plantId }
                        if (matchingPlant != null) {
                            Log.d("DetectDiseaseViewModel", "Plant found in detection ${detection.id}: ${matchingPlant.name}")
                            
                            val imageUri = try {
                                com.gizemir.plantapp.core.util.ImageUtils.filePathToUri(detection.imageUri)
                            } catch (e: Exception) {
                                Log.e("DetectDiseaseViewModel", "Error converting image path to URI", e)
                                null
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                diseaseDetection = detection,
                                selectedImageUri = imageUri
                            )
                            callback(matchingPlant)
                            return@collect
                        }
                    }
                    Log.w("DetectDiseaseViewModel", "Plant not found in any detection: $plantId")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Error loading plant from history", e)
                callback(null)
            }
        }
    }

    fun loadDetectionFromHistory(detectionId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val detection = plantAnalysisRepository.getDiseaseDetectionById(detectionId)
                if (detection != null) {
                    val imageUri = try {
                        com.gizemir.plantapp.core.util.ImageUtils.filePathToUri(detection.imageUri)
                    } catch (e: Exception) {
                        Log.e("DetectDiseaseViewModel", "Error converting image path to URI", e)
                        null
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        diseaseDetection = detection,
                        showResults = true,
                        selectedImageUri = imageUri,
                        errorMessage = null
                    )
                    Log.d("DetectDiseaseViewModel", "Detection loaded from history: ${detection.id}")
                    Log.d("DetectDiseaseViewModel", "Image URI: $imageUri")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Analysis not found",
                        showResults = false
                    )
                }
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Error loading detection from history", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error loading analysis: ${e.localizedMessage}",
                    showResults = false
                )
            }
        }
    }

    fun loadDiseaseFromHistory(diseaseId: String, callback: (com.gizemir.plantapp.domain.model.plant_analysis.Disease?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("DetectDiseaseViewModel", "Loading disease from history: $diseaseId")
                
                val foundDisease = _uiState.value.diseaseDetection?.diseases?.find { it.id == diseaseId }
                if (foundDisease != null) {
                    Log.d("DetectDiseaseViewModel", "Disease found in current detection: ${foundDisease.name}")
                    callback(foundDisease)
                    return@launch
                }
                
                val detections = plantAnalysisRepository.getAllDiseaseDetections()
                detections.collect { detectionList ->
                    for (detection in detectionList) {
                        val matchingDisease = detection.diseases.find { it.id == diseaseId }
                        if (matchingDisease != null) {
                            Log.d("DetectDiseaseViewModel", "Disease found in detection ${detection.id}: ${matchingDisease.name}")
                            
                            // Resim path'ini Uri'ye çevir
                            val imageUri = try {
                                com.gizemir.plantapp.core.util.ImageUtils.filePathToUri(detection.imageUri)
                            } catch (e: Exception) {
                                Log.e("DetectDiseaseViewModel", "Error converting image path to URI", e)
                                null
                            }
                            
                            _uiState.value = _uiState.value.copy(
                                diseaseDetection = detection,
                                selectedImageUri = imageUri
                            )
                            callback(matchingDisease)
                            return@collect
                        }
                    }
                    Log.w("DetectDiseaseViewModel", "Disease not found in any detection: $diseaseId")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e("DetectDiseaseViewModel", "Error loading disease from history", e)
                callback(null)
            }
        }
    }

    fun addPlantToGarden(plantSuggestion: com.gizemir.plantapp.domain.model.plant_analysis.PlantSuggestion) {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
            // Sulama periyodu: watering.min varsa onu kullan, yoksa 7 gün default
            val wateringPeriod = plantSuggestion.watering?.min ?: 7
            val now = System.currentTimeMillis()
            val gardenPlant = GardenPlant(
                userId = currentUser.uid,
                plantId = plantSuggestion.id,
                name = plantSuggestion.commonNames.firstOrNull() ?: plantSuggestion.name,
                imageUrl = plantSuggestion.similarImages.firstOrNull()?.url,
                plantedDate = now,
                lastWateredDate = now,
                wateringPeriodDays = wateringPeriod
            )
            gardenRepository.addPlantToGarden(gardenPlant)
        }
    }
} 