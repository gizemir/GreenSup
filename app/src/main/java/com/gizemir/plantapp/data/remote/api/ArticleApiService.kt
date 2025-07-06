package com.gizemir.plantapp.data.remote.api

import com.gizemir.plantapp.data.remote.dto.article.NewsApiResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArticleApiService {
    
    @GET("everything")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String
    ): Response<NewsApiResponseDto>
    
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("category") category: String? = null,
        @Query("country") country: String = "us",
        @Query("language") language: String = "en",
        @Query("pageSize") pageSize: Int = 20,
        @Query("page") page: Int = 1,
        @Query("apiKey") apiKey: String
    ): Response<NewsApiResponseDto>
    
    companion object {
        const val BASE_URL = com.gizemir.plantapp.core.util.ApiConfig.NEWS_BASE_URL

    }
} 