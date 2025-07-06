package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.core.worker.WateringSchedulerService
import com.gizemir.plantapp.data.repository.auth.AuthRepositoryImpl
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storageRepository: StorageRepository,
        wateringSchedulerService: WateringSchedulerService,
        gardenRepository: GardenRepository
    ): AuthRepository {
        return AuthRepositoryImpl(
            auth,
            firestore,
            storageRepository,
            wateringSchedulerService,
            gardenRepository
        )
    }
}
