package com.gizemir.plantapp.data.remote.api

import com.gizemir.plantapp.data.remote.dto.plant_analysis.PlantIdRequestDto
import com.gizemir.plantapp.data.remote.dto.plant_analysis.PlantIdResponseDto
import retrofit2.Response
import retrofit2.http.*

interface PlantIdApiService {
    
    @POST("identification")
    suspend fun detectDisease(
        @Header("Api-Key") apiKey: String,
        @Query("details") details: String = "common_names,url,name_authority,description,taxonomy,edible_parts,watering,propagation_methods,treatment,cause,wiki_description,synonyms",
        @Body request: PlantIdRequestDto
    ): Response<PlantIdResponseDto>
    
    companion object {
        const val BASE_URL = com.gizemir.plantapp.core.util.ApiConfig.PLANT_ID_BASE_URL
    }
} 