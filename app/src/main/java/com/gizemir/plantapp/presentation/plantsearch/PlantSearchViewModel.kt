package com.gizemir.plantapp.presentation.plantsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.plant_search.Plant

import com.gizemir.plantapp.domain.use_case.plant_search.PlantSearchUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantSearchViewModel @Inject constructor(
    private val plantSearchUseCases: PlantSearchUseCases
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlantSearchUiState())
    val uiState: StateFlow<PlantSearchUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun searchPlants(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(plants = emptyList())
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            plantSearchUseCases.searchPlantsUseCase(query).fold(
                onSuccess = { plants ->
                    _uiState.value = _uiState.value.copy(
                        plants = plants,
                        isLoading = false,
                        errorMessage = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        plants = emptyList(),
                        isLoading = false,
                        errorMessage = exception.message ?: "Bilinmeyen bir hata oluştu"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

data class PlantSearchUiState(
    val plants: List<Plant> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
