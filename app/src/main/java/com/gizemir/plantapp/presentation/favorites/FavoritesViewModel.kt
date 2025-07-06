package com.gizemir.plantapp.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gizemir.plantapp.domain.model.favorite.FavoritePlant
import com.gizemir.plantapp.domain.model.favorite.FavoriteSource
import com.gizemir.plantapp.domain.repository.favorite.FavoriteRepository
import com.gizemir.plantapp.domain.use_case.favorites.GetFavoritesUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    init {
        migrateOldFavorites()
        loadFavorites()
        migrateMiscategorizedFavorites()
    }
    
    private fun migrateOldFavorites() {
        viewModelScope.launch {
            try {
                favoriteRepository.migrateUnknownUserFavorites()
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Migration error: ${e.message}")
            }
        }
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    getFavoritesUseCase(currentUser.uid).collectLatest { favorites ->
                        val plantFavorites = favorites.filter { 
                            it.source in listOf(
                                FavoriteSource.PLANT_SEARCH, 
                                FavoriteSource.PLANT_ID_DETAIL
                            ) 
                        }
                        val diseaseFavorites = favorites.filter { 
                            it.source == FavoriteSource.DISEASE_ANALYSIS 
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            allFavorites = favorites,
                            plantFavorites = plantFavorites,
                            diseaseFavorites = diseaseFavorites,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        allFavorites = emptyList(),
                        plantFavorites = emptyList(),
                        diseaseFavorites = emptyList(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Favoriler yüklenirken hata oluştu"
                )
            }
        }
    }
    
    private fun migrateMiscategorizedFavorites() {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser ?: return@launch
                
                var hasProcessed = false
                getFavoritesUseCase(currentUser.uid).collectLatest { favorites ->
                    // Sadece bir kere çalıştır
                    if (hasProcessed) return@collectLatest
                    hasProcessed = true
                    
                    val toMigrate = favorites.filter { favorite ->
                        favorite.source == FavoriteSource.PLANT_ID_DETAIL && 
                        isPlantDiseaseRelated(favorite.commonName, favorite.scientificName)
                    }
                    
                    if (toMigrate.isNotEmpty()) {
                        android.util.Log.d("FavoritesViewModel", "Found ${toMigrate.size} items to migrate to Disease category")
                        
                        toMigrate.forEach { favorite ->
                            val updatedFavorite = favorite.copy(
                                source = FavoriteSource.DISEASE_ANALYSIS,
                                family = "Pathogen"
                            )
                            favoriteRepository.removeFromFavorites(currentUser.uid, favorite.plantId)
                            favoriteRepository.addToFavorites(updatedFavorite)
                            android.util.Log.d("FavoritesViewModel", "Migrated ${favorite.commonName} to Disease category")
                        }
                        
                        android.util.Log.d("FavoritesViewModel", "Migration completed: ${toMigrate.size} items moved to Disease category")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("FavoritesViewModel", "Migration error: ${e.message}")
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
    
    fun selectCategory(category: FavoriteCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
    
    fun removeFromFavorites(plantId: Int) {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    favoriteRepository.removeFromFavorites(currentUser.uid, plantId)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Favorilerden kaldırılırken hata oluştu"
                )
            }
        }
    }
    
    fun clearAllFavorites() {
        viewModelScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    when (_uiState.value.selectedCategory) {
                        FavoriteCategory.PLANTS -> {
                            _uiState.value.plantFavorites.forEach { favorite ->
                                favoriteRepository.removeFromFavorites(currentUser.uid, favorite.plantId)
                            }
                        }
                        FavoriteCategory.DISEASES -> {
                            _uiState.value.diseaseFavorites.forEach { favorite ->
                                favoriteRepository.removeFromFavorites(currentUser.uid, favorite.plantId)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Favoriler temizlenirken hata oluştu"
                )
            }
        }
    }
    
    fun refreshFavorites() {
        loadFavorites()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

enum class FavoriteCategory {
    PLANTS,
    DISEASES
}

data class FavoritesUiState(
    val allFavorites: List<FavoritePlant> = emptyList(),
    val plantFavorites: List<FavoritePlant> = emptyList(),
    val diseaseFavorites: List<FavoritePlant> = emptyList(),
    val selectedCategory: FavoriteCategory = FavoriteCategory.PLANTS,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val currentFavorites: List<FavoritePlant>
        get() = when (selectedCategory) {
            FavoriteCategory.PLANTS -> plantFavorites
            FavoriteCategory.DISEASES -> diseaseFavorites
        }
}

