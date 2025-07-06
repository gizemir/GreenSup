package com.gizemir.plantapp.domain.repository.user

import com.gizemir.plantapp.domain.model.user.User
import android.net.Uri

interface UserRepository {
    suspend fun getUserById(userId: String): User
    suspend fun getCurrentUser(): User
    suspend fun updateUserProfile(name: String, bio: String): User
    suspend fun updateProfilePicture(imageUri: Uri): String
}
