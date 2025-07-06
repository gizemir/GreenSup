package com.gizemir.plantapp.data.repository.plant_analysis

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.gizemir.plantapp.core.util.ApiConfig
import com.gizemir.plantapp.data.local.dao.plant_analysis.PlantAnalysisDao
import com.gizemir.plantapp.data.local.entity.plant_analysis.toEntity
import com.gizemir.plantapp.data.mapper.plant_analysis.toDomainModel
import com.gizemir.plantapp.data.remote.api.PlantIdApiService
import com.gizemir.plantapp.data.remote.dto.plant_analysis.PlantIdRequestDto
import com.gizemir.plantapp.domain.model.plant_analysis.DiseaseDetection
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import com.gizemir.plantapp.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class PlantAnalysisRepositoryImpl @Inject constructor(
    private val apiService: PlantIdApiService,
    private val dao: PlantAnalysisDao,
    private val context: Context
) : PlantAnalysisRepository {

    override suspend fun detectDisease(imageUri: Uri): Resource<DiseaseDetection> {
        return identifyWithHealthAnalysis(imageUri, "all")
    }

    override suspend fun identifyPlantOnly(imageUri: Uri): Resource<DiseaseDetection> {
        return identifyWithHealthAnalysis(imageUri, "auto")
    }

    override suspend fun detectDiseaseOnly(imageUri: Uri): Resource<DiseaseDetection> {
        return identifyWithHealthAnalysis(imageUri, "only")
    }

    private suspend fun identifyWithHealthAnalysis(imageUri: Uri, healthMode: String): Resource<DiseaseDetection> {
        return try {
            Log.d("DetectDiseaseRepository", "Starting ${when(healthMode) {
                "auto" -> "plant identification"
                "only" -> "disease detection" 
                else -> "plant analysis"
            }}")
            Log.d("DetectDiseaseRepository", "Image URI: $imageUri")
            Log.d("DetectDiseaseRepository", "Health mode: $healthMode")

            val base64Image = uriToBase64(imageUri)
            Log.d("DetectDiseaseRepository", "Base64 image length: ${base64Image.length}")

            val request = PlantIdRequestDto(
                images = listOf(base64Image),
                health = healthMode,
                classificationLevel = "species",
                classificationRaw = true,
                symptoms = if (healthMode == "only") true else null,
                similarImages = true
            )

            Log.d("DetectDiseaseRepository", "Making API call")
            Log.d("DetectDiseaseRepository", "Request body: health=${request.health}, classificationLevel=${request.classificationLevel}, symptoms=${request.symptoms}")

            val response = apiService.detectDisease(
                apiKey = ApiConfig.PLANT_ID_API_KEY,
                details = "common_names,url,name_authority,description,taxonomy,edible_parts,watering,propagation_methods,treatment,cause,wiki_description,synonyms",
                request = request
            )

            Log.d("DetectDiseaseRepository", "API Response: Success=${response.isSuccessful}, Code=${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Log.d("DetectDiseaseRepository", "API Response Body: $responseBody")
                Log.d("DetectDiseaseRepository", "Classification suggestions: ${responseBody.result.classification?.suggestions}")
                Log.d("DetectDiseaseRepository", "Plant probability: ${responseBody.result.isPlant.probability}")
                val diseaseDetection = responseBody.toDomainModel(imageUri.toString())
                Log.d("DetectDiseaseRepository", "Analysis successful: ${diseaseDetection.id}")
                Log.d("DetectDiseaseRepository", "Plant suggestions count: ${diseaseDetection.plantSuggestions.size}")
                Log.d("DetectDiseaseRepository", "Disease suggestions count: ${diseaseDetection.diseases.size}")
                diseaseDetection.plantSuggestions.forEachIndexed { index, plant ->
                    Log.d("DetectDiseaseRepository", "Plant $index: ${plant.name} (${plant.probability})")
                }
                diseaseDetection.diseases.forEachIndexed { index, disease ->
                    Log.d("DetectDiseaseRepository", "Disease $index: ${disease.name} (${disease.probability})")
                }
                Resource.Success(diseaseDetection)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DetectDiseaseRepository", "API error: ${response.code()} - ${response.message()} - $errorBody")
                Resource.Error("Analysis failed: ${response.message()}")
            }

        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Exception in analysis", e)
            Resource.Error("Analysis error: ${e.localizedMessage}")
        }
    }

    override suspend fun saveDiseaseDetection(detection: DiseaseDetection, userId: String?) {
        try {
            val persistentImagePath = try {
                val originalUri = Uri.parse(detection.imageUri)
                if (detection.imageUri.startsWith("content://")) {
                    com.gizemir.plantapp.core.util.ImageUtils.copyUriToInternalStorage(
                        context, 
                        originalUri, 
                        "disease_analysis"
                    )
                } else {
                    detection.imageUri
                }
            } catch (e: Exception) {
                Log.e("DetectDiseaseRepository", "Error copying image to internal storage", e)
                detection.imageUri
            }
            
            val updatedDetection = detection.copy(imageUri = persistentImagePath ?: detection.imageUri)
            
            dao.insertDiseaseDetection(updatedDetection.toEntity(userId))
            Log.d("DetectDiseaseRepository", "Disease detection saved with persistent image: ${updatedDetection.id}")
            Log.d("DetectDiseaseRepository", "Image path: ${updatedDetection.imageUri}")
        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Error saving disease detection", e)
        }
    }

    override suspend fun getDiseaseDetectionById(id: String): DiseaseDetection? {
        return try {
            dao.getDiseaseDetectionById(id)?.toDomainModel()
        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Error getting disease detection by id", e)
            null
        }
    }

    override fun getAllDiseaseDetections(): Flow<List<DiseaseDetection>> {
        return dao.getAllDiseaseDetections().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDiseaseDetectionsByUser(userId: String): Flow<List<DiseaseDetection>> {
        return dao.getDiseaseDetectionsByUser(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun deleteDiseaseDetection(id: String) {
        try {
            dao.deleteDiseaseDetectionById(id)
            Log.d("DetectDiseaseRepository", "Disease detection deleted: $id")
        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Error deleting disease detection", e)
        }
    }

    override suspend fun deleteAllDiseaseDetections() {
        try {
            dao.deleteAllDiseaseDetections()
            Log.d("DetectDiseaseRepository", "All disease detections deleted")
        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Error deleting all disease detections", e)
        }
    }

    override suspend fun clearAllDiseaseDetections(userId: String?) {
        try {
            if (userId != null) {
                dao.deleteAllDiseaseDetectionsByUser(userId)
                Log.d("DetectDiseaseRepository", "All disease detections cleared for user: $userId")
            } else {
                dao.deleteAllDiseaseDetections()
                Log.d("DetectDiseaseRepository", "All disease detections cleared")
            }
        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Error clearing all disease detections", e)
        }
    }

    private fun uriToBase64(uri: Uri): String {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes != null) {
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val imageType = when {
                    mimeType.contains("png") -> "image/png"
                    mimeType.contains("jpg") || mimeType.contains("jpeg") -> "image/jpeg"
                    else -> "image/jpeg"
                }
                

                val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:$imageType;base64,$base64String"
            } else {
                throw Exception("Could not read image from URI")
            }
        } catch (e: Exception) {
            Log.e("DetectDiseaseRepository", "Error converting URI to base64", e)
            throw e
        }
    }
} 