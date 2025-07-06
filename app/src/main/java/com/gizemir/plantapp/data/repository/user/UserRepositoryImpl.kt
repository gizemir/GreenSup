package com.gizemir.plantapp.data.repository.user

import android.net.Uri
import android.util.Log
import com.gizemir.plantapp.domain.model.user.User
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.gizemir.plantapp.domain.repository.user.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageRepository: StorageRepository
) : UserRepository {

    private val usersCollection = firestore.collection("users")
    
    override suspend fun getUserById(userId: String): User {
        try {
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) {
                Log.w("UserRepository", "User document not found for ID: $userId, creating from Firebase Auth")
                return createUserFromFirebaseAuth(userId)
            }
            
            return userDoc.toObject(User::class.java)?.copy(id = userId)
                ?: throw Exception("Could not parse user data")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user: ${e.message}")
            try {
                return createUserFromFirebaseAuth(userId)
            } catch (authException: Exception) {
                Log.e("UserRepository", "Failed to create user from Firebase Auth: ${authException.message}")
                throw Exception("User not found and could not be created: ${e.message}")
            }
        }
    }
    
    private suspend fun createUserFromFirebaseAuth(userId: String): User {
        val firebaseUser = if (auth.currentUser?.uid == userId) {
            auth.currentUser
        } else {
            null
        }
        
        val userName = when {
            firebaseUser?.displayName?.isNotBlank() == true -> firebaseUser.displayName!!
            firebaseUser?.email?.isNotBlank() == true -> firebaseUser.email!!.substringBefore('@')
            else -> "Anonim Kullanıcı"
        }
        
        val user = User(
            id = userId,
            email = firebaseUser?.email ?: "",
            name = userName,
            profilePictureUrl = firebaseUser?.photoUrl?.toString(),
            bio = null,
            plantCount = 0
        )
        
        try {
            usersCollection.document(userId).set(user).await()
            Log.i("UserRepository", "User document created successfully for ID: $userId with name: $userName")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to save user document: ${e.message}")
        }
        
        return user
    }
    
    override suspend fun getCurrentUser(): User {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        return getUserById(currentUser.uid)
    }
    
    override suspend fun updateUserProfile(name: String, bio: String): User {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        
        try {
            val updates = mapOf(
                "name" to name,
                "bio" to bio
            )
            
            usersCollection.document(currentUser.uid).update(updates).await()
            return getUserById(currentUser.uid)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile: ${e.message}")
            throw e
        }
    }
    
    override suspend fun updateProfilePicture(imageUri: Uri): String {
        val currentUser = auth.currentUser ?: throw Exception("No user logged in")
        
        try {
            val userDoc = usersCollection.document(currentUser.uid).get().await()
            val currentPhotoUrl = userDoc.getString("profilePictureUrl")
            
            if (!currentPhotoUrl.isNullOrBlank()) {
                storageRepository.deleteImage(currentPhotoUrl)
            }
            
            val newPhotoUrl = storageRepository.uploadImage(imageUri, "profile_pictures")
            
            usersCollection.document(currentUser.uid)
                .update("profilePictureUrl", newPhotoUrl)
                .await()
            
            return newPhotoUrl
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile picture: ${e.message}")
            throw e
        }
    }
}
