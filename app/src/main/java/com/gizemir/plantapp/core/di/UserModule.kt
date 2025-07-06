package com.gizemir.plantapp.core.di

import com.gizemir.plantapp.data.repository.user.UserRepositoryImpl
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.gizemir.plantapp.domain.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storageRepository: StorageRepository
    ): UserRepository {
        return UserRepositoryImpl(firestore, auth, storageRepository)
    }
}
