package com.gizemir.plantapp.data.repository.auth

import android.net.Uri
import android.util.Log
import com.gizemir.plantapp.core.worker.WateringSchedulerService
import com.gizemir.plantapp.domain.model.user.User
import com.gizemir.plantapp.domain.repository.auth.AuthRepository
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.gizemir.plantapp.domain.repository.garden.GardenRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository,
    private val wateringSchedulerService: WateringSchedulerService,
    private val gardenRepository: GardenRepository
) : AuthRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun login(email: String, password: String): User {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Authentication failed")
        
        val userDoc = usersCollection.document(userId).get().await()
        
        if (!userDoc.exists()) {
            val userName = auth.currentUser?.displayName ?: email.substringBefore('@')
            val newUser = User(id = userId, email = email, name = userName)
            usersCollection.document(userId).set(newUser).await()
            return newUser
        }
        
        return userDoc.toObject(User::class.java)?.copy(id = userId)
            ?: throw Exception("Failed to parse user data")
    }

    override suspend fun register(name: String, email: String, password: String): User {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw Exception("Registration failed")
        
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        auth.currentUser?.updateProfile(profileUpdates)?.await()
        
        val newUser = User(id = userId, email = email, name = name)
        usersCollection.document(userId).set(newUser).await()
        
        return newUser
    }

    override suspend fun getCurrentUser(): User? {
        val currentUser = auth.currentUser ?: return null
        val userId = currentUser.uid
        
        Log.d("AuthRepository", "Getting current user with ID: $userId")
        
        try {
            val userDoc = usersCollection.document(userId).get().await()
            return if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)?.copy(id = userId)
                Log.d("AuthRepository", "User found in Firestore: ${user?.name}")
                user
            } else {
                Log.w("AuthRepository", "User document not found, creating new one")
                val userName = when {
                    currentUser.displayName?.isNotBlank() == true -> currentUser.displayName!!
                    currentUser.email?.isNotBlank() == true -> currentUser.email!!.substringBefore('@')
                    else -> "Kullanıcı"
                }
                
                val basicUser = User(
                    id = userId,
                    email = currentUser.email ?: "",
                    name = userName,
                    profilePictureUrl = currentUser.photoUrl?.toString()
                )
                usersCollection.document(userId).set(basicUser).await()
                Log.i("AuthRepository", "New user document created: $userName")
                basicUser
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting current user: ${e.message}")
            
            return try {
                User(
                    id = userId,
                    email = currentUser.email ?: "",
                    name = currentUser.displayName ?: currentUser.email?.substringBefore('@') ?: "Kullanıcı",
                    profilePictureUrl = currentUser.photoUrl?.toString()
                )
            } catch (fallbackException: Exception) {
                Log.e("AuthRepository", "Fallback user creation failed: ${fallbackException.message}")
                null
            }
        }
    }

    override suspend fun logout() {
        try {
            wateringSchedulerService.cancelAllWateringReminders()
            Log.d("AuthRepository", "Cancelled all watering reminders on logout")

            gardenRepository.clearGardenData()
            Log.d("AuthRepository", "Cleared local garden data on logout")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during logout cleanup", e)
        }
        
        auth.signOut()
    }

    override suspend fun updateProfile(name: String, bio: String): User {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        
        if (name.isNotBlank()) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            currentUser.updateProfile(profileUpdates).await()
        }
        
        val updates = mapOf(
            "name" to name,
            "bio" to bio
        )
        
        usersCollection.document(currentUser.uid).update(updates).await()
        
        val userDoc = usersCollection.document(currentUser.uid).get().await()
        return userDoc.toObject(User::class.java)?.copy(id = currentUser.uid)
            ?: throw Exception("Failed to update profile")
    }

    override suspend fun updateProfilePicture(imageUri: Uri): String {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(imageUri)
            .build()
        currentUser.updateProfile(profileUpdates).await()
        
        val imageUrl = storageRepository.uploadImage(imageUri, "profile_pictures")
        usersCollection.document(currentUser.uid)
            .update("profilePictureUrl", imageUrl)
            .await()
            
        return imageUrl
    }

    override suspend fun updatePassword(currentPassword: String, newPassword: String) {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        val email = currentUser.email ?: throw Exception("User email not available")
        
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        currentUser.reauthenticate(credential).await()
        
        currentUser.updatePassword(newPassword).await()
    }
}

