package com.gizemir.plantapp.presentation.plant_analysis.state

import android.net.Uri
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection

data class PlantAnalysisState(
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val diseaseDetection: DiseaseDetection? = null,
    val showResults: Boolean = false
) 