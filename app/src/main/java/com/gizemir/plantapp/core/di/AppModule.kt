package com.gizemir.plantapp.core.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import coil.ImageLoader
import coil.util.Logger // Bu satırı ekleyin - DebugLogger için gerekli
import com.gizemir.plantapp.R
import com.gizemir.plantapp.core.util.ApiConfig
import com.gizemir.plantapp.data.local.dao.weather.WeatherDao
import com.gizemir.plantapp.data.local.dao.weather.DayWeatherDao
import com.gizemir.plantapp.data.remote.api.WeatherApi
import com.gizemir.plantapp.data.repository.weather.PreferencesRepositoryImpl
import com.gizemir.plantapp.data.repository.weather.WeatherRepositoryImpl
import com.gizemir.plantapp.domain.repository.weather.PreferencesRepository
import com.gizemir.plantapp.domain.repository.weather.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import java.net.InetAddress
import java.net.UnknownHostException

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "PlantApp/1.0 (Android)")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .dns(Dns.SYSTEM)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .followRedirects(true)
            .followSslRedirects(true)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherApi(okHttpClient: OkHttpClient): WeatherApi {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConfig.WEATHER_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("plant_app_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(sharedPreferences: SharedPreferences): PreferencesRepository {
        return PreferencesRepositoryImpl(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        api: WeatherApi,
        weatherDao: WeatherDao,
        dayWeatherDao: DayWeatherDao
    ): WeatherRepository {
        return WeatherRepositoryImpl(api, ApiConfig.WEATHER_API_KEY, weatherDao, dayWeatherDao)
    }

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context, okHttpClient: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .okHttpClient { 
                okHttpClient.newBuilder()
                    .hostnameVerifier { _, _ -> true }
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .build()
            }
            .placeholder(R.drawable.ic_weather_placeholder)
            .error(R.drawable.ic_weather_placeholder)
            .logger(DebugLogger()) // Hata ayıklama için logger ekliyoruz
            .build()
    }
}

class CustomDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            // Önce sistem DNS'ini dene
            Dns.SYSTEM.lookup(hostname)
        } catch (e: UnknownHostException) {
            try {
                when (hostname) {
                    "perenual.com" -> {
                        listOf(InetAddress.getByName("162.0.208.55"))
                    }
                    "api.perenual.com" -> {
                        listOf(InetAddress.getByName("162.0.208.55"))
                    }
                    "newsapi.org" -> {
                        try {
                            listOf(
                                InetAddress.getByName("104.21.68.158"),
                                InetAddress.getByName("172.67.153.161")
                            )
                        } catch (e2: Exception) {
                            listOf(InetAddress.getByName("8.8.8.8"))
                        }
                    }
                    "api.openweathermap.org" -> {
                        listOf(InetAddress.getByName("3.213.179.74"))
                    }
                    else -> {
                        try {
                            InetAddress.getAllByName(hostname).toList()
                        } catch (e2: UnknownHostException) {
                            try {
                                InetAddress.getAllByName(hostname).toList()
                            } catch (e3: UnknownHostException) {
                                Log.w("CustomDns", "All DNS resolution methods failed for $hostname")
                                emptyList()
                            }
                        }
                    }
                }
            } catch (e2: Exception) {
                Log.e("CustomDns", "DNS resolution failed for $hostname", e2)
                emptyList()
            }
        }
    }
}

class DebugLogger : Logger {
    override var level: Int = Log.INFO

    override fun log(tag: String, priority: Int, message: String?, throwable: Throwable?) {
        if (priority >= level) {
            Log.println(priority, "CoilImageLoader", "$tag: $message")
            throwable?.let { Log.e("CoilImageLoader", "Exception:", it) }
        }
    }
}
