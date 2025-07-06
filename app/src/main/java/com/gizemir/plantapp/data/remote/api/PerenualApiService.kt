package com.gizemir.plantapp.data.remote.api

import com.gizemir.plantapp.data.remote.dto.plant_search.PerenualPlantDetailResponseDto
import com.gizemir.plantapp.data.remote.dto.plant_search.PerenualPlantSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PerenualApiService {
    
    @GET("species-list")
    suspend fun searchPlants(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): Response<PerenualPlantSearchResponseDto>
    
    @GET("species/details/{id}")
    suspend fun getPlantDetail(
        @Path("id") plantId: Int,
        @Query("key") apiKey: String
    ): Response<PerenualPlantDetailResponseDto>
    
    companion object {
        const val BASE_URL = com.gizemir.plantapp.core.util.ApiConfig.PERENUAL_BASE_URL
    }
} 