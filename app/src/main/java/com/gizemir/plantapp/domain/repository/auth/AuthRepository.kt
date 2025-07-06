package com.gizemir.plantapp.domain.repository.auth

import android.net.Uri
import com.gizemir.plantapp.domain.model.user.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String): User
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    suspend fun updateProfile(name: String, bio: String): User
    suspend fun updateProfilePicture(imageUri: Uri): String
    suspend fun updatePassword(currentPassword: String, newPassword: String)
}

