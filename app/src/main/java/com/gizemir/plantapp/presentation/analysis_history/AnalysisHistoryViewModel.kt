package com.gizemir.plantapp.presentation.analysis_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.use_case.plant_analysis.GetPlantAnalysisUseCase
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisHistoryViewModel @Inject constructor(
    private val getPlantAnalysisUseCase: GetPlantAnalysisUseCase,
    private val plantAnalysisRepository: PlantAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisHistoryUiState())
    val uiState: StateFlow<AnalysisHistoryUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisHistory()
    }

    private fun loadAnalysisHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    getPlantAnalysisUseCase(currentUser.uid).collect { analyses ->
                        val sortedAnalyses = analyses.sortedByDescending { it.analyzedAt }
                        val healthyAnalyses = sortedAnalyses.filter { it.isHealthy }
                        val diseasedAnalyses = sortedAnalyses.filter { !it.isHealthy }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            allAnalyses = sortedAnalyses,
                            healthyAnalyses = healthyAnalyses,
                            diseasedAnalyses = diseasedAnalyses,
                            error = null
                        )
                    }
                } else {
                    getPlantAnalysisUseCase().collect { analyses ->
                        val sortedAnalyses = analyses.sortedByDescending { it.analyzedAt }
                        val healthyAnalyses = sortedAnalyses.filter { it.isHealthy }
                        val diseasedAnalyses = sortedAnalyses.filter { !it.isHealthy }
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            allAnalyses = sortedAnalyses,
                            healthyAnalyses = healthyAnalyses,
                            diseasedAnalyses = diseasedAnalyses,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun selectCategory(category: AnalysisCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun refreshHistory() {
        loadAnalysisHistory()
    }

    fun deleteAnalysis(id: String) {
        viewModelScope.launch {
            try {
                plantAnalysisRepository.deleteDiseaseDetection(id)
                refreshHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error deleting analysis"
                )
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                plantAnalysisRepository.clearAllDiseaseDetections(currentUser?.uid)
                refreshHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error clearing all history"
                )
            }
        }
    }

    fun clearCategoryHistory() {
        viewModelScope.launch {
            try {
                val analysesToDelete = when (_uiState.value.selectedCategory) {
                    AnalysisCategory.HEALTHY -> _uiState.value.healthyAnalyses
                    AnalysisCategory.DISEASED -> _uiState.value.diseasedAnalyses
                }
                
                analysesToDelete.forEach { analysis ->
                    plantAnalysisRepository.deleteDiseaseDetection(analysis.id)
                }
                
                refreshHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error clearing category history"
                )
            }
        }
    }
}

enum class AnalysisCategory {
    HEALTHY,
    DISEASED
}

data class AnalysisHistoryUiState(
    val isLoading: Boolean = false,
    val allAnalyses: List<DiseaseDetection> = emptyList(),
    val healthyAnalyses: List<DiseaseDetection> = emptyList(),
    val diseasedAnalyses: List<DiseaseDetection> = emptyList(),
    val selectedCategory: AnalysisCategory = AnalysisCategory.HEALTHY,
    val error: String? = null
) {
    val currentAnalyses: List<DiseaseDetection>
        get() = when (selectedCategory) {
            AnalysisCategory.HEALTHY -> healthyAnalyses
            AnalysisCategory.DISEASED -> diseasedAnalyses
        }
    
    val analyses: List<DiseaseDetection>
        get() = allAnalyses
} 