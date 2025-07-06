package com.gizemir.plantapp.data.repository.forum

import android.net.Uri
import android.util.Log
import com.gizemir.plantapp.domain.repository.forum.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class StorageRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRepository {

    override suspend fun uploadImage(imageUri: Uri, folderPath: String): String {
        try {
            val filename = UUID.randomUUID().toString()
            val ref = storage.reference.child("$folderPath/$filename")
            
            val uploadTask = ref.putFile(imageUri).await()
            return ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("StorageRepository", "Error uploading image: ${e.message}")
            throw e
        }
    }
    
    override suspend fun deleteImage(imageUrl: String) {
        try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            Log.e("StorageRepository", "Error deleting image: ${e.message}")
            throw e
        }
    }
}
