package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.remote.api.PerenualApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlantSearchNetworkModule {

    @Provides
    @Singleton
    fun providePerenualApiService(okHttpClient: OkHttpClient): PerenualApiService {
        return Retrofit.Builder()
            .baseUrl(PerenualApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PerenualApiService::class.java)
    }
}
