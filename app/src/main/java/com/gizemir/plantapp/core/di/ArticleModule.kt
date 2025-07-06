package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.remote.api.ArticleApiService
import com.gizemir.plantapp.data.repository.article.ArticleRepositoryImpl
import com.gizemir.plantapp.domain.repository.article.ArticleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ArticleOkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ArticleModule {

    @Provides
    @Singleton
    @ArticleOkHttpClient
    fun provideArticleOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "PlantApp/1.0 (Android)")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .dns(SimpleDns())
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(180, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                var response = chain.proceed(chain.request())
                var tryCount = 0
                val maxTries = 3
                
                while (!response.isSuccessful && tryCount < maxTries) {
                    tryCount++
                    android.util.Log.d("ArticleOkHttp", "Request failed, retrying... ($tryCount/$maxTries)")
                    response.close()
                    response = chain.proceed(chain.request())
                }
                
                response
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleApiService(@ArticleOkHttpClient okHttpClient: OkHttpClient): ArticleApiService {
        return Retrofit.Builder()
            .baseUrl(ArticleApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArticleApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideArticleRepository(articleApiService: ArticleApiService): ArticleRepository {
        return ArticleRepositoryImpl(articleApiService)
    }
}

class SimpleDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            InetAddress.getAllByName(hostname).toList()
        } catch (e: UnknownHostException) {
            try {
                android.util.Log.d("SimpleDns", "System DNS failed for $hostname, trying manual resolution")
                when (hostname) {
                    "newsapi.org" -> {
                        listOf(
                            InetAddress.getByName("104.21.68.158"),
                            InetAddress.getByName("172.67.153.161")
                        )
                    }
                    else -> {
                        android.util.Log.d("SimpleDns", "Trying to resolve $hostname manually")
                        listOf(InetAddress.getByName(hostname))
                    }
                }
            } catch (e2: Exception) {
                android.util.Log.e("SimpleDns", "All DNS resolution failed for $hostname", e2)
                emptyList()
            }
        }
    }
} 