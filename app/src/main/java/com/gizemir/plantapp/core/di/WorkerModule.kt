package com.gizemir.plantapp.core.di

import android.content.Context
import androidx.work.WorkManager
import com.gizemir.plantapp.core.worker.WateringSchedulerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideWateringSchedulerService(
        workManager: WorkManager
    ): WateringSchedulerService {
        return WateringSchedulerService(workManager)
    }
} 