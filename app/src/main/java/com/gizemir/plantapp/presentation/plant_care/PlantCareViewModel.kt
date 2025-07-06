package com.gizemir.plantapp.presentation.plant_care

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.data.local.cache.CacheManager
import com.gizemir.plantapp.core.util.RateLimiter
import com.gizemir.plantapp.domain.model.plant_care.PlantCare
import com.gizemir.plantapp.domain.use_case.plant_care.GetPlantCareUseCase
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantCareViewModel @Inject constructor(
    private val getPlantCareUseCase: GetPlantCareUseCase,
    private val rateLimiter: RateLimiter,
    private val cacheManager: CacheManager
) : ViewModel() {

    private val _uiState = mutableStateOf(PlantCareUiState())
    val uiState: State<PlantCareUiState> = _uiState

    fun loadPlantCare(plantName: String, scientificName: String) {
        viewModelScope.launch {
            Log.i("PlantCareViewModel", "🎯 Loading plant care for: $plantName ($scientificName)")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null,
                rateLimitInfo = "Remaining requests: ${rateLimiter.getRemainingRequests()}"
            )
            
            getPlantCareUseCase(plantName, scientificName).fold(
                onSuccess = { plantCare ->
                    Log.i("PlantCareViewModel", "🎉 SUCCESS! Plant care loaded successfully for: $plantName")
                    Log.d("PlantCareViewModel", "📋 Sections loaded: watering, lighting, soil, temperature, humidity, problems, tips")
                    
                    val cacheInfo = cacheManager.getPlantCareCacheInfo()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        plantCare = plantCare,
                        error = null,
                        cacheInfo = cacheInfo,
                        rateLimitInfo = "Remaining requests: ${rateLimiter.getRemainingRequests()}"
                    )
                },
                onFailure = { exception ->
                    Log.e("PlantCareViewModel", "❌ ERROR loading plant care for $plantName: ${exception.message}")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error",
                        rateLimitInfo = "Remaining requests: ${rateLimiter.getRemainingRequests()}"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearCache() {
        viewModelScope.launch {
            cacheManager.clearPlantCareCache()
            Log.i("PlantCareViewModel", "🗑️ Cache cleared for testing")
        }
    }
}

data class PlantCareUiState(
    val isLoading: Boolean = false,
    val plantCare: PlantCare? = null,
    val error: String? = null,
    val cacheInfo: String = "",
    val rateLimitInfo: String = ""
) 