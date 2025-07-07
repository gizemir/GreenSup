package com.gizemir.plantapp.data.local.cache

import android.util.Log
import com.gizemir.plantapp.data.local.dao.forum.CommentDao
import com.gizemir.plantapp.data.local.dao.forum.PostDao
import com.gizemir.plantapp.data.local.dao.plant_care.PlantCareDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantDetailDao
import com.gizemir.plantapp.data.local.dao.plant_search.PlantSearchQueryDao
import com.gizemir.plantapp.data.local.dao.weather.DayWeatherDao
import com.gizemir.plantapp.data.local.dao.weather.WeatherDao
import com.gizemir.plantapp.data.local.entity.plant_care.PlantCareEntity
import com.gizemir.plantapp.domain.model.plant_care.PlantCare
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(
    private val plantDao: PlantDao,
    private val plantDetailDao: PlantDetailDao,
    private val plantSearchQueryDao: PlantSearchQueryDao,
    private val weatherDao: WeatherDao,
    private val dayWeatherDao: DayWeatherDao,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val plantCareDao: PlantCareDao
) {
    
    companion object {
        private const val TAG = "CacheManager"
        private const val CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 saat
        private const val WEATHER_CACHE_EXPIRY_TIME = 30 * 60 * 1000L // 30 dakika
        private const val FORUM_CACHE_EXPIRY_TIME = 30 * 60 * 1000L // 30 dakika
        private const val PLANT_CARE_CACHE_EXPIRY_TIME = 24 * 60 * 60 * 1000L // 24 saat
    }
    private val plantCareMutex = Mutex()
    private val plantCareCache = mutableMapOf<String, PlantCareEntry>()
    data class PlantCareEntry(
        val plantCare: PlantCare,
        val timestamp: Long
    )
    

    suspend fun getPlantCare(plantName: String, scientificName: String): PlantCare? {
        return try {

            val entity = plantCareDao.getPlantCare(plantName, scientificName)
            if (entity != null) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - entity.timestamp < PLANT_CARE_CACHE_EXPIRY_TIME) {
                    entity.toPlantCare()
                } else {

                    plantCareDao.deletePlantCare(entity)
                    null
                }
            } else {

                plantCareMutex.withLock {
                    val key = createPlantCareKey(plantName, scientificName)
                    val entry = plantCareCache[key]
                    
                    if (entry != null) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - entry.timestamp < PLANT_CARE_CACHE_EXPIRY_TIME) {
                            entry.plantCare
                        } else {

                            plantCareCache.remove(key)
                            null
                        }
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "PlantCare cache okuma hatası: ${e.message}")
            null
        }
    }
    

    suspend fun putPlantCare(plantName: String, scientificName: String, plantCare: PlantCare) {
        try {

            val entity = PlantCareEntity.fromPlantCare(plantCare)
            plantCareDao.insertPlantCare(entity)
            

            plantCareMutex.withLock {
                val key = createPlantCareKey(plantName, scientificName)
                plantCareCache[key] = PlantCareEntry(
                    plantCare = plantCare,
                    timestamp = System.currentTimeMillis()
                )
                

                cleanupExpiredPlantCareEntries()
            }
        } catch (e: Exception) {
            Log.e(TAG, "PlantCare cache kaydetme hatası: ${e.message}")
        }
    }
    

    suspend fun clearPlantCareCache() {
        try {
            // Database'den temizle
            plantCareDao.deleteAllPlantCare()
            
            // In-memory cache'i temizle
            plantCareMutex.withLock {
                plantCareCache.clear()
            }
        } catch (e: Exception) {
            Log.e(TAG, "PlantCare cache temizleme hatası: ${e.message}")
        }
    }
    

    suspend fun getPlantCareCacheSize(): Int {
        return try {
            // Database'deki toplam sayı + in-memory cache sayısı
            val dbCount = plantCareDao.getPlantCareCount()
            val memoryCount = plantCareMutex.withLock { plantCareCache.size }
            maxOf(dbCount, memoryCount) // En büyüğünü al (çakışma olabilir)
        } catch (e: Exception) {
            Log.e(TAG, "PlantCare cache boyutu alma hatası: ${e.message}")
            0
        }
    }
    

    suspend fun getPlantCareCacheInfo(): String {
        return plantCareMutex.withLock {
            val totalEntries = plantCareCache.size
            val currentTime = System.currentTimeMillis()
            val validEntries = plantCareCache.count { (_, entry) ->
                currentTime - entry.timestamp < PLANT_CARE_CACHE_EXPIRY_TIME
            }
            "PlantCare Cache: $validEntries/$totalEntries geçerli giriş"
        }
    }
    
    private fun createPlantCareKey(plantName: String, scientificName: String): String {
        return "${plantName.lowercase().trim()}_${scientificName.lowercase().trim()}"
    }
    
    private fun cleanupExpiredPlantCareEntries() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = plantCareCache.filter { (_, entry) ->
            currentTime - entry.timestamp >= PLANT_CARE_CACHE_EXPIRY_TIME
        }.keys
        
        expiredKeys.forEach { key ->
            plantCareCache.remove(key)
        }
    }


    suspend fun clearExpiredCache() {
        try {
            val currentTime = System.currentTimeMillis()
            val plantExpireTime = currentTime - CACHE_EXPIRY_TIME
            val weatherExpireTime = currentTime - WEATHER_CACHE_EXPIRY_TIME
            val forumExpireTime = currentTime - FORUM_CACHE_EXPIRY_TIME
            plantDao.deleteExpiredPlants(plantExpireTime)
            plantDetailDao.deleteExpiredPlantDetails(plantExpireTime)
            plantSearchQueryDao.deleteExpiredQueries(plantExpireTime)
            weatherDao.deleteExpiredWeather(weatherExpireTime)
            dayWeatherDao.deleteExpiredForecast(weatherExpireTime)
            postDao.deleteExpiredPosts(forumExpireTime)
            commentDao.deleteExpiredComments(forumExpireTime)
            val plantCareExpireTime = currentTime - PLANT_CARE_CACHE_EXPIRY_TIME
            plantCareDao.deleteExpiredPlantCare(plantCareExpireTime)
            plantCareMutex.withLock {
                cleanupExpiredPlantCareEntries()
            }
            
            Log.d(TAG, "Süresi dolmuş cache'ler temizlendi")
        } catch (e: Exception) {
            Log.e(TAG, "Cache temizleme hatası: ${e.message}")
        }
    }

    suspend fun clearAllCache() {
        try {

            plantDao.deleteAllPlants()
            plantDetailDao.deleteAllPlantDetails()
            plantSearchQueryDao.deleteAllQueries()
            weatherDao.deleteAllWeather()
            dayWeatherDao.deleteAllForecast()

            postDao.deleteAllPosts()
            commentDao.deleteAllComments()
            clearPlantCareCache()
            
            Log.d(TAG, "Tüm cache temizlendi")
        } catch (e: Exception) {
            Log.e(TAG, "Cache temizleme hatası: ${e.message}")
        }
    }

    suspend fun logCacheStats() {
        try {
            val plantCount = plantDao.getPlantCount()
            val plantDetailCount = plantDetailDao.getPlantDetailCount()
            val weatherCount = weatherDao.getRecentWeather(Int.MAX_VALUE).size
            val forecastCount = dayWeatherDao.getForecastByCity("").size
            val postCount = postDao.getPostCount()
            val commentCount = commentDao.getCommentCountByPostId("")
            val plantCareCount = getPlantCareCacheSize()
            val plantCareInfo = getPlantCareCacheInfo()
            
            Log.d(TAG, "Cache İstatistikleri:")
            Log.d(TAG, "- Plants: $plantCount")
            Log.d(TAG, "- Plant Details: $plantDetailCount")
            Log.d(TAG, "- Weather: $weatherCount")
            Log.d(TAG, "- Forecasts: $forecastCount")
            Log.d(TAG, "- Posts: $postCount")
            Log.d(TAG, "- Comments: $commentCount")
            Log.d(TAG, "- Plant Care: $plantCareCount")
            Log.d(TAG, "- $plantCareInfo")
        } catch (e: Exception) {
            Log.e(TAG, "Cache istatistik hatası: ${e.message}")
        }
    }

    fun startAutoCacheCleanup(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            clearExpiredCache()
            logCacheStats()
        }
    }

    suspend fun clearWeatherCacheForCity(city: String) {
        try {
            weatherDao.getWeatherByCity(city)?.let {
                // Implement individual city deletion if needed
            }
            dayWeatherDao.deleteForecastByCity(city)
            Log.d(TAG, "Weather cache temizlendi - City: $city")
        } catch (e: Exception) {
            Log.e(TAG, "Weather cache temizleme hatası: ${e.message}")
        }
    }

    suspend fun manageCacheSize() {
        try {
            val plantCount = plantDao.getPlantCount()
            val plantDetailCount = plantDetailDao.getPlantDetailCount()
            val plantCareCount = getPlantCareCacheSize()
            
            // Çok fazla veri varsa eski olanları temizle
            if (plantCount > 1000) {
                val expireTime = System.currentTimeMillis() - (12 * 60 * 60 * 1000L) // 12 saat
                plantDao.deleteExpiredPlants(expireTime)
                Log.d(TAG, "Plant cache boyutu azaltıldı")
            }
            
            if (plantDetailCount > 500) {
                val expireTime = System.currentTimeMillis() - (6 * 60 * 60 * 1000L) // 6 saat
                plantDetailDao.deleteExpiredPlantDetails(expireTime)
                Log.d(TAG, "Plant detail cache boyutu azaltıldı")
            }
            
            if (plantCareCount > 200) {
                // Database'den eski kayıtları temizle
                val expireTime = System.currentTimeMillis() - (12 * 60 * 60 * 1000L) // 12 saat
                plantCareDao.deleteExpiredPlantCare(expireTime)
                
                // In-memory cache'i temizle
                plantCareMutex.withLock {
                    cleanupExpiredPlantCareEntries()
                }
                Log.d(TAG, "Plant care cache boyutu azaltıldı")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Cache boyut yönetimi hatası: ${e.message}")
        }
    }
} 