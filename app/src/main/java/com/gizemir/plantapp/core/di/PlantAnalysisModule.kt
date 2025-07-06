package com.gizemir.plantapp.core.di

import android.content.Context
import androidx.room.Room
import com.gizemir.plantapp.data.local.PlantAppDatabase
import com.gizemir.plantapp.data.local.dao.plant_analysis.PlantAnalysisDao
import com.gizemir.plantapp.data.remote.api.PlantIdApiService
import com.gizemir.plantapp.data.repository.plant_analysis.PlantAnalysisRepositoryImpl
import com.gizemir.plantapp.domain.repository.plant_analysis.PlantAnalysisRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlantAnalysisModule {

    @Provides
    @Singleton
    fun providePlantAnalysisDao(database: PlantAppDatabase): PlantAnalysisDao {
        return database.plantAnalysisDao()
    }

    @Provides
    @Singleton
    @Named("plant_id")
    fun providePlantIdOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    @Named("plant_id")
    fun providePlantIdRetrofit(@Named("plant_id") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.gizemir.plantapp.core.util.ApiConfig.PLANT_ID_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePlantIdApiService(@Named("plant_id") retrofit: Retrofit): PlantIdApiService {
        return retrofit.create(PlantIdApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlantAnalysisRepository(
        apiService: PlantIdApiService,
        dao: PlantAnalysisDao,
        @ApplicationContext context: Context
    ): PlantAnalysisRepository {
        return PlantAnalysisRepositoryImpl(apiService, dao, context)
    }
} 