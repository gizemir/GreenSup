package com.gizemir.plantapp.data.repository.plant_search

import com.gizemir.plantapp.data.local.dao.plant_search.PlantDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantDetailDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantSearchQueryDao
import com.gizemir.plantapp.data.local.entity.plant_search.PlantEntity
import com.gizemir.plantapp.data.local.entity.plant_search.PlantDetailEntity
import com.gizemir.plantapp.data.local.entity.plant_search.PlantSearchQueryEntity
import com.gizemir.plantapp.data.remote.api.PerenualApiService
import com.gizemir.plantapp.domain.model.plant_search.Plant
import com.gizemir.plantapp.domain.model.plant_search.PlantDetail
import com.gizemir.plantapp.domain.repository.plant_search.PlantSearchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantSearchRepositoryImpl @Inject constructor(
    private val apiService: PerenualApiService,
    private val plantDao: PlantDao,
    private val plantDetailDao: PlantDetailDao,
    private val plantSearchQueryDao: PlantSearchQueryDao
) : PlantSearchRepository {
    
    companion object {
        private const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 saat
    }
    
    override suspend fun searchPlants(query: String, page: Int): Result<List<Plant>> {
        return try {
            val cachedQuery = plantSearchQueryDao.getSearchQuery(query, page)
            val currentTime = System.currentTimeMillis()
            
            if (cachedQuery != null && (currentTime - cachedQuery.cached_at) < CACHE_EXPIRY_TIME) {
                val cachedPlants = plantDao.getPlantsByIds(cachedQuery.plantIds)
                if (cachedPlants.isNotEmpty()) {
                    val plants = cachedPlants.map { it.toPlant() }
                    return Result.success(plants)
                }
            }
            
            val response = apiService.searchPlants(
                apiKey = com.gizemir.plantapp.core.util.ApiConfig.PERENUAL_API_KEY,
                query = query,
                page = page
            )
            
            if (response.isSuccessful) {
                val plants = response.body()?.data?.map { plantDto -> 
                    plantDto.toPlant() 
                } ?: emptyList()
                
                if (plants.isNotEmpty()) {
                    savePlantsToCache(plants, query, page)
                }
                
                Result.success(plants)
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("PlantRepository", "Arama API hatası: ${response.code()} - ${response.message()} - $errorBody")
                
                when (response.code()) {
                    429 -> {
                        android.util.Log.w("PlantRepository", "Arama rate limit aşıldı, cache'den döndürülüyor")
                        val cachedQuery = plantSearchQueryDao.getSearchQuery(query, page)
                        if (cachedQuery != null) {
                            val cachedPlants = plantDao.getPlantsByIds(cachedQuery.plantIds)
                            if (cachedPlants.isNotEmpty()) {
                                return Result.success(cachedPlants.map { it.toPlant() })
                            }
                        }
                        Result.failure(Exception("Rate limit aşıldı ve cache'de veri bulunamadı"))
                    }
                    401, 403 -> {
                        android.util.Log.w("PlantRepository", "API key problemi, cache'den döndürülüyor")
                        val cachedQuery = plantSearchQueryDao.getSearchQuery(query, page)
                        if (cachedQuery != null) {
                            val cachedPlants = plantDao.getPlantsByIds(cachedQuery.plantIds)
                            if (cachedPlants.isNotEmpty()) {
                                return Result.success(cachedPlants.map { it.toPlant() })
                            }
                        }
                        Result.failure(Exception("API key problemi ve cache'de veri bulunamadı"))
                    }
                    else -> {
                        Result.failure(Exception("Arama başarısız: ${response.code()} - ${response.message()}"))
                    }
                }
            }
        } catch (e: Exception) {
            val cachedQuery = plantSearchQueryDao.getSearchQuery(query, page)
            if (cachedQuery != null) {
                val cachedPlants = plantDao.getPlantsByIds(cachedQuery.plantIds)
                if (cachedPlants.isNotEmpty()) {
                    val plants = cachedPlants.map { it.toPlant() }
                    return Result.success(plants)
                }
            }
            
            Result.failure(Exception("Bağlantı hatası: ${e.message}"))
        }
    }
    
    override suspend fun getPlantDetail(plantId: Int): Result<PlantDetail> {
        return try {
            val cachedDetail = plantDetailDao.getPlantDetailById(plantId)
            val currentTime = System.currentTimeMillis()
            
            if (cachedDetail != null && (currentTime - cachedDetail.cached_at) < CACHE_EXPIRY_TIME) {
                android.util.Log.d("PlantRepository", "Cache'den plant detail döndürülüyor: ID $plantId")
                return Result.success(cachedDetail.toPlantDetail())
            }
            
            android.util.Log.d("PlantRepository", "API çağrısı yapılıyor: ID $plantId")
            val response = apiService.getPlantDetail(
                plantId = plantId,
                apiKey = com.gizemir.plantapp.core.util.ApiConfig.PERENUAL_API_KEY
            )
            
            android.util.Log.d("PlantRepository", "API Response: Success=${response.isSuccessful}, Code=${response.code()}")
            
            if (response.isSuccessful) {
                val responseBody = response.body()
                android.util.Log.d("PlantRepository", "Response body null mu: ${responseBody == null}")
                
                val plantDetail = responseBody?.toPlantDetail()
                
                if (plantDetail != null) {
                    android.util.Log.d("PlantRepository", "Gerçek API verisi kullanılıyor: ${plantDetail.commonName}")
                    
                    val enhancedPlantDetail = enhancePlantDetailWithFallbacks(plantDetail)
                    
                    val entity = PlantDetailEntity.fromPlantDetail(enhancedPlantDetail)
                    plantDetailDao.insertPlantDetail(entity)
                    
                    Result.success(enhancedPlantDetail)
                } else {
                    android.util.Log.w("PlantRepository", "PlantDetail null, mock data döndürülüyor")
                    val mockPlantDetail = createMockPlantDetail(plantId)
                    Result.success(mockPlantDetail)
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("PlantRepository", "API hatası: ${response.code()} - ${response.message()} - $errorBody")
                
                if (cachedDetail != null) {
                    android.util.Log.d("PlantRepository", "API hatası, eski cache döndürülüyor")
                    return Result.success(cachedDetail.toPlantDetail())
                }
                
                when (response.code()) {
                    401, 403 -> {
                        android.util.Log.w("PlantRepository", "API key problemi (${response.code()}), mock data döndürülüyor")
                        val mockPlantDetail = createMockPlantDetail(plantId)
                        Result.success(mockPlantDetail)
                    }
                    429 -> {
                        android.util.Log.w("PlantRepository", "API rate limit aşıldı (${response.code()}), mock data döndürülüyor")
                        android.util.Log.i("PlantRepository", "Rate limit bilgisi: $errorBody")
                        val mockPlantDetail = createMockPlantDetail(plantId)
                        Result.success(mockPlantDetail)
                    }
                    else -> {
                        Result.failure(Exception("Detay alma başarısız: ${response.code()} - ${response.message()} - $errorBody"))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PlantRepository", "Exception: ${e.message}")
            val cachedDetail = plantDetailDao.getPlantDetailById(plantId)
            if (cachedDetail != null) {
                android.util.Log.d("PlantRepository", "Network hatası, cache döndürülüyor")
                return Result.success(cachedDetail.toPlantDetail())
            }
            
            android.util.Log.w("PlantRepository", "Network hatası, mock data döndürülüyor")
            val mockPlantDetail = createMockPlantDetail(plantId)
            Result.success(mockPlantDetail)
        }
    }
    
    private suspend fun savePlantsToCache(plants: List<Plant>, query: String, page: Int) {
        try {
            val plantEntities = plants.map { PlantEntity.fromPlant(it) }
            plantDao.insertPlants(plantEntities)
            
            val plantIds = plants.map { it.id }
            val queryEntity = PlantSearchQueryEntity(
                query = query,
                page = page,
                plantIds = plantIds
            )
            plantSearchQueryDao.insertSearchQuery(queryEntity)
        } catch (e: Exception) {
        }
    }
    
    suspend fun clearExpiredCache() {
        try {
            val expireTime = System.currentTimeMillis() - CACHE_EXPIRY_TIME
            plantDao.deleteExpiredPlants(expireTime)
            plantDetailDao.deleteExpiredPlantDetails(expireTime)
            plantSearchQueryDao.deleteExpiredQueries(expireTime)
        } catch (e: Exception) {
        }
    }
    
    suspend fun clearAllCache() {
        try {
            plantDao.deleteAllPlants()
            plantDetailDao.deleteAllPlantDetails()
            plantSearchQueryDao.deleteAllQueries()
        } catch (e: Exception) {
        }
    }
    
    private fun enhancePlantDetailWithFallbacks(plantDetail: PlantDetail): PlantDetail {
        android.util.Log.d("PlantRepository", "🔧 Enhancing plant detail with fallbacks...")
        
        val enhancedDistribution = if (plantDetail.distribution == null ||
            (plantDetail.distribution!!.native.isNullOrEmpty() && 
             plantDetail.distribution!!.introduced.isNullOrEmpty())) {
            android.util.Log.d("PlantRepository", "📍 Adding fallback distribution data")
            com.gizemir.plantapp.domain.model.plant_search.PlantDistribution(
                native = listOf("Habitat information not available"),
                introduced = null,
                doubtful = null,
                absent = null,
                extinct = null
            )
        } else {
            plantDetail.distribution
        }
        
        val enhancedCareInfo = if (plantDetail.careInfo == null ||
            (plantDetail.careInfo!!.sunlight.isNullOrEmpty() && 
             plantDetail.careInfo!!.watering.isNullOrBlank())) {
            android.util.Log.d("PlantRepository", "🌿 Adding fallback care information")
            com.gizemir.plantapp.domain.model.plant_search.PlantCareInfo(
                sunlight = listOf("Moderate sunlight"),
                watering = "Regular watering",
                wateringDays = "7-10",
                pruningMonths = listOf("Spring"),
                pruningFrequency = "As needed"
            )
        } else {
            plantDetail.careInfo
        }
        

        val enhancedFamily = plantDetail.family ?: "Family information not available"
        
        return plantDetail.copy(
            family = enhancedFamily,
            distribution = enhancedDistribution,
            careInfo = enhancedCareInfo
        )
    }
    
    private fun createMockPlantDetail(plantId: Int): PlantDetail {
        return PlantDetail(
            id = plantId,
            commonName = "Sample Plant #$plantId",
            scientificName = "Plantus exemplarius $plantId",
            imageUrl = "https://perenual.com/storage/species_image/1_abies_alba/og/49255769768_df55596553_b.jpg",
            family = "Plantaceae",
            genus = "Plantus", 
            author = "Botanicus Exemplarius",
            bibliography = "Botanical Journal of Examples, Vol. 1",
            year = 2024,
            status = "accepted",
            rank = "species",
            familyCommonName = "Example Plant Family",
            genusId = 1,
            familyId = 1,
            synonyms = listOf("Alternative Name 1", "Alternative Name 2", "Synonym plantus"),
            hardinessMapUrl = "https://example.com/hardiness-map.jpg",
            distribution = com.gizemir.plantapp.domain.model.plant_search.PlantDistribution(
                native = listOf("Mediterranean", "Southern Europe", "North Africa"),
                introduced = listOf("North America", "Australia"),
                doubtful = listOf("Central Asia"),
                absent = null,
                extinct = null
            ),
            careInfo = com.gizemir.plantapp.domain.model.plant_search.PlantCareInfo(
                sunlight = listOf("full sun", "part shade"),
                watering = "Frequent",
                wateringDays = "5-7",
                pruningMonths = listOf("March", "April"),
                pruningFrequency = "1 times yearly"
            )
        )
    }
}
