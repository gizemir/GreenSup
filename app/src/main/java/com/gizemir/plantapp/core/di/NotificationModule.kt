package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.repository.notification.NotificationRepositoryImpl
import com.gizemir.plantapp.domain.repository.notification.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): NotificationRepository {
        return NotificationRepositoryImpl(firestore, auth)
    }
} 