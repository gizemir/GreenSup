package com.gizemir.plantapp.domain.repository.forum

import android.net.Uri

interface StorageRepository {
    suspend fun uploadImage(imageUri: Uri, folderPath: String): String
    suspend fun deleteImage(imageUrl: String)
}
