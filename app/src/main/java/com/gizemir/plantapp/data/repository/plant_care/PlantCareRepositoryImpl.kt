package com.gizemir.plantapp.data.repository.plant_care

import android.util.Log
import com.gizemir.plantapp.core.util.ApiConfig
import com.gizemir.plantapp.data.local.cache.CacheManager
import com.gizemir.plantapp.core.util.RateLimiter
import com.gizemir.plantapp.core.util.RetryHelper
import com.gizemir.plantapp.data.remote.api.GeminiApiService
import com.gizemir.plantapp.data.remote.dto.plant_care.ContentDto
import com.gizemir.plantapp.data.remote.dto.plant_care.GeminiRequestDto
import com.gizemir.plantapp.data.remote.dto.plant_care.PartDto
import com.gizemir.plantapp.domain.model.plant_care.PlantCare
import com.gizemir.plantapp.domain.model.plant_care.PlantCareSection
import com.gizemir.plantapp.domain.repository.plant_care.PlantCareRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlantCareRepositoryImpl @Inject constructor(
    private val geminiApiService: GeminiApiService,
    private val rateLimiter: RateLimiter,
    private val cacheManager: CacheManager
) : PlantCareRepository {

    override suspend fun getPlantCareInfo(plantName: String, scientificName: String): Result<PlantCare> {
        return try {
            Log.i("PlantCareRepository", "🌱 Starting plant care request for: $plantName ($scientificName)")
            
            val cachedResult = cacheManager.getPlantCare(plantName, scientificName)
            if (cachedResult != null) {
                Log.i("PlantCareRepository", "✅ Cache HIT for $plantName ($scientificName) - Using cached data")
                return Result.success(cachedResult)
            }
            
            Log.i("PlantCareRepository", "❌ Cache MISS for $plantName ($scientificName) - Will fetch from API")
            
            rateLimiter.waitForRateLimit()
            
            val plantCare = RetryHelper.retryWithExponentialBackoff(
                maxRetries = 3,
                initialDelayMs = 2000, // Start with 2 seconds for rate limiting
                maxDelayMs = 30000, // Max 30 seconds
                retryOn = { throwable ->
                    when {
                        RetryHelper.isRateLimitError(throwable) -> {
                            Log.w("PlantCareRepository", "Rate limit hit, will retry with longer delay")
                            true
                        }
                        RetryHelper.isNetworkError(throwable) -> {
                            Log.w("PlantCareRepository", "Network error, will retry")
                            true
                        }
                        RetryHelper.isServerError(throwable) -> {
                            Log.w("PlantCareRepository", "Server error, will retry")
                            true
                        }
                        else -> false
                    }
                }
            ) {
                makeGeminiApiCall(plantName, scientificName)
            }
            
            cacheManager.putPlantCare(plantName, scientificName, plantCare)
            Log.i("PlantCareRepository", "💾 Successfully cached result for $plantName ($scientificName)")
            Log.i("PlantCareRepository", "🎉 Plant care request completed successfully for: $plantName")
            
            Result.success(plantCare)
            
        } catch (e: Exception) {
            Log.e("PlantCareRepository", "❌ FINAL ERROR after retries: ${e.message}")
            val errorMessage = RetryHelper.getErrorMessage(e)
            
            val mockData = createMockPlantCare(plantName, scientificName)
            Log.w("PlantCareRepository", "⚠️ FALLBACK: Returning mock data due to error: $errorMessage")
            Log.i("PlantCareRepository", "🔄 Mock data created for: $plantName - User will still see content")
            
            Result.success(mockData)
        }
    }
    
    private suspend fun makeGeminiApiCall(plantName: String, scientificName: String): PlantCare {
        val prompt = createPlantCarePrompt(plantName, scientificName)
        
        val request = GeminiRequestDto(
            contents = listOf(
                ContentDto(
                    parts = listOf(
                        PartDto(text = prompt)
                    )
                )
            )
        )
        
        Log.i("PlantCareRepository", "🚀 Sending request to Gemini API for: $plantName ($scientificName)")
        Log.i("PlantCareRepository", "🔄 Rate limiter status: ${rateLimiter.getRemainingRequests()} requests remaining")
        Log.d("PlantCareRepository", "📝 Prompt length: ${prompt.length} characters")
        
        val startTime = System.currentTimeMillis()
        
        val response = geminiApiService.generateContent(
            apiKey = ApiConfig.GEMINI_API_KEY,
            request = request
        )
        
        val responseTime = System.currentTimeMillis() - startTime
        Log.i("PlantCareRepository", "⏱️ Gemini API response time: ${responseTime}ms")
        
        if (response.isSuccessful) {
            val responseBody = response.body()
            val aiText = responseBody?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (aiText != null) {
                Log.i("PlantCareRepository", "✅ SUCCESS! Gemini API responded successfully")
                Log.i("PlantCareRepository", "📊 Response length: ${aiText.length} characters")
                Log.d("PlantCareRepository", "🧠 AI Response preview: ${aiText.take(400)}...")
                Log.d("PlantCareRepository", "📝 Full AI Response structure check:")
                Log.d("PlantCareRepository", "WATERING_SECTION position: ${aiText.indexOf("WATERING_SECTION:")}")
                Log.d("PlantCareRepository", "WATERING_TIPS position: ${aiText.indexOf("WATERING_TIPS:")}")
                
                val plantCare = parseAiResponseToPlantCare(aiText, plantName, scientificName)
                Log.i("PlantCareRepository", "✅ AI response successfully parsed into PlantCare object")
                return plantCare
            } else {
                Log.w("PlantCareRepository", "⚠️ Gemini response is empty - no content in response")
                throw RuntimeException("Empty response from Gemini API")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("PlantCareRepository", "❌ GEMINI API ERROR: HTTP ${response.code()}")
            Log.e("PlantCareRepository", "❌ Error details: $errorBody")
            throw RuntimeException("Gemini API error: ${response.code()}")
        }
    }
    
    private fun createPlantCarePrompt(plantName: String, scientificName: String): String {
        return """
        Provide specific care instructions for $plantName ($scientificName) plant. This plant has unique needs different from other plants. Focus on the specific requirements of this exact species.

        Format your response EXACTLY like this:

        WATERING_SECTION:
        [Write specific watering needs for this plant - how often, how much, special considerations]
        WATERING_TIPS:
        - [Specific tip 1 for this plant]
        - [Specific tip 2 for this plant] 
        - [Specific tip 3 for this plant]
        - [Specific tip 4 for this plant]

        LIGHTING_SECTION:
        [Write 1-2 clear sentences about light requirements]
        LIGHTING_TIPS:
        - [Tip 1]
        - [Tip 2]
        - [Tip 3] 
        - [Tip 4]

        SOIL_SECTION:
        [Write 1-2 clear sentences about soil requirements]
        SOIL_TIPS:
        - [Tip 1]
        - [Tip 2]
        - [Tip 3]
        - [Tip 4]

        TEMPERATURE_SECTION:
        [Write 1-2 clear sentences about temperature needs]
        TEMPERATURE_TIPS:
        - [Tip 1]
        - [Tip 2]
        - [Tip 3]
        - [Tip 4]

        HUMIDITY_SECTION:
        [Write 1-2 clear sentences about humidity requirements]
        HUMIDITY_TIPS:
        - [Tip 1]
        - [Tip 2]
        - [Tip 3]
        - [Tip 4]

        PROBLEMS_SECTION:
        [Write 1-2 clear sentences about common problems]
        PROBLEMS_TIPS:
        - [Tip 1]
        - [Tip 2]
        - [Tip 3]
        - [Tip 4]

        GENERAL_TIPS:
        - [General tip 1]
        - [General tip 2]
        - [General tip 3]
        - [General tip 4]
        - [General tip 5]

        IMPORTANT: 
        - Give SPECIFIC advice for $plantName ($scientificName), not generic plant care
        - Each plant has different needs - focus on this exact species
        - Keep sentences short, clear and practical
        - Avoid repetition and generic advice
        - Follow the format exactly with section markers
        """.trimIndent()
    }
    
    private fun parseAiResponseToPlantCare(aiText: String, plantName: String, scientificName: String): PlantCare {
        return try {
            Log.d("PlantCareRepository", "🔍 Parsing AI response for: $plantName")
            
            PlantCare(
                plantName = plantName,
                scientificName = scientificName,
                watering = parseStructuredSection(aiText, "WATERING", "Watering"),
                lighting = parseStructuredSection(aiText, "LIGHTING", "Lighting"),
                soil = parseStructuredSection(aiText, "SOIL", "Soil"),
                temperature = parseStructuredSection(aiText, "TEMPERATURE", "Temperature"),
                humidity = parseStructuredSection(aiText, "HUMIDITY", "Humidity"),
                commonProblems = parseStructuredSection(aiText, "PROBLEMS", "Common Problems"),
                generalTips = parseGeneralTips(aiText)
            )
        } catch (e: Exception) {
            Log.w("PlantCareRepository", "⚠️ AI response parsing failed, using mock data: ${e.message}")
            Log.d("PlantCareRepository", "🔄 Falling back to mock data for: $plantName")
            createMockPlantCare(plantName, scientificName)
        }
    }
    
    private fun parseStructuredSection(text: String, sectionKey: String, title: String): PlantCareSection {
        return try {
            val sectionPattern = "${sectionKey}_SECTION:"
            val tipsPattern = "${sectionKey}_TIPS:"
            
            val sectionStart = text.indexOf(sectionPattern)
            val tipsStart = text.indexOf(tipsPattern)
            
            Log.d("PlantCareRepository", "Parsing $title: sectionStart=$sectionStart, tipsStart=$tipsStart")
            
            if (sectionStart == -1 || tipsStart == -1) {
                Log.w("PlantCareRepository", "Section markers not found for $title")
                return getDefaultSection(title, sectionKey)
            }
            
            val descriptionStart = sectionStart + sectionPattern.length
            val descriptionEnd = tipsStart
            
            if (descriptionStart >= descriptionEnd) {
                Log.w("PlantCareRepository", "Invalid section boundaries for $title: start=$descriptionStart, end=$descriptionEnd")
                return getDefaultSection(title, sectionKey)
            }
            
            val descriptionText = text.substring(descriptionStart, descriptionEnd).trim()
            
            val tipsStart_pos = tipsStart + tipsPattern.length
            val nextSectionStart = findNextSectionStart(text, tipsStart)
            
            val tipsText = if (nextSectionStart != -1 && nextSectionStart > tipsStart_pos) {
                text.substring(tipsStart_pos, nextSectionStart)
            } else {
                text.substring(tipsStart_pos)
            }.trim()
            
            val tips = extractTipsList(tipsText)
            
            Log.d("PlantCareRepository", "✅ Parsed $title: ${tips.size} tips extracted")
            Log.d("PlantCareRepository", "📝 $title Description: ${cleanDescription(descriptionText).take(100)}...")
            Log.d("PlantCareRepository", "📋 $title Tips: ${tips.joinToString(" | ")}")
            
            val finalTips = tips.ifEmpty { getDefaultTips(sectionKey) }
            if (tips.isEmpty()) {
                Log.w("PlantCareRepository", "⚠️ No tips found for $title, using defaults")
            }
            
            PlantCareSection(
                title = title,
                description = cleanDescription(descriptionText),
                tips = finalTips
            )
            
        } catch (e: Exception) {
            Log.w("PlantCareRepository", "⚠️ Failed to parse $title section: ${e.message}")
            getDefaultSection(title, sectionKey)
        }
    }
    
    private fun findNextSectionStart(text: String, currentPos: Int): Int {
        val sectionKeywords = listOf("_SECTION:", "_TIPS:", "GENERAL_TIPS:")
        var nextPos = text.length
        
        for (keyword in sectionKeywords) {
            val pos = text.indexOf(keyword, currentPos + 1)
            if (pos != -1 && pos < nextPos) {
                nextPos = pos
            }
        }
        
        return if (nextPos == text.length) -1 else nextPos
    }
    
    private fun extractTipsList(tipsText: String): List<String> {
        return tipsText.lines()
            .filter { it.trim().startsWith("-") }
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotBlank() && it.length > 5 }
            .take(4) // Maximum 4 tips per section
    }
    
    private fun parseGeneralTips(text: String): List<String> {
        return try {
            val generalTipsStart = text.indexOf("GENERAL_TIPS:")
            if (generalTipsStart == -1) {
                return getDefaultGeneralTips()
            }
            
            val tipsText = text.substring(generalTipsStart + "GENERAL_TIPS:".length).trim()
            val tips = extractTipsList(tipsText)
            
            Log.d("PlantCareRepository", "✅ Parsed general tips: ${tips.size} tips extracted")
            
            tips.ifEmpty { getDefaultGeneralTips() }
        } catch (e: Exception) {
            Log.w("PlantCareRepository", "⚠️ Failed to parse general tips: ${e.message}")
            getDefaultGeneralTips()
        }
    }
    
    private fun cleanDescription(description: String): String {
        return description.lines()
            .filter { it.trim().isNotBlank() }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(200)
    }
    
    private fun getDefaultSection(title: String, sectionKey: String): PlantCareSection {
        Log.w("PlantCareRepository", "🔄 Using DEFAULT data for $title section")
        return PlantCareSection(
            title = title,
            description = getDefaultDescription(title),
            tips = getDefaultTips(sectionKey)
        )
    }
    
    private fun getDefaultDescription(title: String): String {
        return when (title.lowercase()) {
            "watering" -> "This plant has moderate water needs. Water when the soil surface becomes dry."
            "lighting" -> "This plant prefers bright, indirect light. Protect from direct sunlight."
            "soil" -> "Use well-draining soil with good organic content. pH should be neutral."
            "temperature" -> "Keep in moderate temperatures between 18-24°C. Protect from extreme changes."
            "humidity" -> "Maintain moderate humidity levels. Use humidity trays if needed."
            "common problems" -> "Watch for common issues like overwatering, pests, and leaf problems."
            else -> "$title requirements for optimal plant health."
        }
    }
    
    private fun getDefaultTips(sectionKey: String): List<String> {
        return when (sectionKey.uppercase()) {
            "WATERING" -> listOf("Water 1-2 times per week", "Check soil moisture first", "Avoid overwatering", "Water in morning hours")
            "LIGHTING" -> listOf("Provide bright indirect light", "Keep away from direct sun", "Rotate occasionally", "Ensure 6-8 hours daily")
            "SOIL" -> listOf("Use well-draining mix", "Add organic matter", "Check pH levels", "Repot annually")
            "TEMPERATURE" -> listOf("Keep at 18-24°C", "Avoid sudden changes", "Protect from drafts", "Monitor seasonal needs")
            "HUMIDITY" -> listOf("Maintain 50-60% humidity", "Use humidity tray", "Group with other plants", "Mist occasionally")
            "PROBLEMS" -> listOf("Check for pests regularly", "Remove dead leaves", "Watch for disease signs", "Maintain good hygiene")
            else -> listOf("Follow care guidelines", "Monitor plant health", "Be consistent", "Observe and adjust")
        }
    }
    
    private fun getDefaultGeneralTips(): List<String> {
        return listOf(
            "Be patient with your plant's growth",
            "Maintain consistent care routine",
            "Observe and learn from your plant",
            "Keep tools clean and sterile",
            "Adjust care with seasons"
        )
    }
    
    private fun createMockPlantCare(plantName: String, scientificName: String): PlantCare {
        return PlantCare(
            plantName = plantName,
            scientificName = scientificName,
            watering = PlantCareSection(
                title = "Watering",
                description = "$plantName is a plant with moderate water needs. Watering when the soil surface is dry is the ideal approach.",
                tips = listOf(
                    "Water 1-2 times per week",
                    "Check soil dryness",
                    "Avoid overwatering",
                    "Water in the morning hours"
                )
            ),
            lighting = PlantCareSection(
                title = "Lighting",
                description = "This plant loves bright, indirect light. Direct sunlight can cause burns on the leaves.",
                tips = listOf(
                    "Provide bright indirect light",
                    "Keep 1-2 meters away from south-facing windows",
                    "Protect from direct sun",
                    "Ensure 6-8 hours of light per day"
                )
            ),
            soil = PlantCareSection(
                title = "Soil",
                description = "Use well-draining soil rich in organic matter. pH level should be between 6.0-7.0.",
                tips = listOf(
                    "Use well-draining mix",
                    "Add perlite or vermiculite",
                    "Apply liquid fertilizer monthly",
                    "Repot once a year"
                )
            ),
            temperature = PlantCareSection(
                title = "Temperature",
                description = "Temperatures between 18-24°C are ideal. Should be protected from sudden temperature changes.",
                tips = listOf(
                    "Keep in 18-24°C temperature range",
                    "Protect from sudden changes",
                    "Keep away from air conditioning",
                    "Keep in warm areas during winter"
                )
            ),
            humidity = PlantCareSection(
                title = "Humidity",
                description = "50-60% humidity is the ideal level. Take humidity-increasing measures in dry environments.",
                tips = listOf(
                    "Use humidity tray",
                    "Wipe leaves with slightly damp cloth",
                    "Group with other plants",
                    "Monitor with humidity meter"
                )
            ),
            commonProblems = PlantCareSection(
                title = "Common Problems",
                description = "Leaf yellowing, brown spots, and pests are the most common problems encountered.",
                tips = listOf(
                    "Remove yellowing leaves",
                    "Watch for signs of overwatering",
                    "Check for pests regularly",
                    "Detect disease symptoms early"
                )
            ),
            generalTips = listOf(
                "$plantName requires patient care",
                "Learn the plant's needs through regular observation",
                "Perform care using clean tools",
                "Minimize stress factors",
                "Consider seasonal care changes"
            )
        )
    }
} 