package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.repository.forum.PostRepositoryImpl
import com.gizemir.plantapp.data.repository.forum.StorageRepositoryImpl
import com.gizemir.plantapp.domain.repository.forum.PostRepository
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.gizemir.plantapp.domain.repository.notification.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideStorageRepository(
        storage: FirebaseStorage
    ): StorageRepository = StorageRepositoryImpl(storage)
    
    @Provides
    @Singleton
    fun providePostRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storageRepository: StorageRepository,
        notificationRepository: NotificationRepository
    ): PostRepository = PostRepositoryImpl(firestore, auth, storageRepository, notificationRepository)
}

