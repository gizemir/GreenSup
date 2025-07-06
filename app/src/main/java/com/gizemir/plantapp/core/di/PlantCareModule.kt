package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.core.util.ApiConfig
import com.gizemir.plantapp.data.remote.api.GeminiApiService
import com.gizemir.plantapp.data.repository.plant_care.PlantCareRepositoryImpl
import com.gizemir.plantapp.domain.repository.plant_care.PlantCareRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlantCareModule {

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiRetrofit(@Named("gemini") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(@Named("gemini") retrofit: Retrofit): GeminiApiService {
        return retrofit.create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlantCareRepository(
        geminiApiService: GeminiApiService,
        rateLimiter: com.gizemir.plantapp.core.util.RateLimiter,
        cacheManager: com.gizemir.plantapp.data.local.cache.CacheManager
    ): PlantCareRepository {
        return PlantCareRepositoryImpl(geminiApiService, rateLimiter, cacheManager)
    }
} 