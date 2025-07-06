package com.gizemir.plantapp.data.remote.api

import com.gizemir.plantapp.data.remote.dto.plant_care.GeminiRequestDto
import com.gizemir.plantapp.data.remote.dto.plant_care.GeminiResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    @POST("v1/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequestDto
    ): Response<GeminiResponseDto>
} 