package com.gizemir.plantapp.core.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {
    

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "plant_image"): String? {
        return try {
            Log.d("ImageUtils", "Copying URI to internal storage: $sourceUri")
            
            val imagesDir = File(context.filesDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val destFile = File(imagesDir, fileName)
            
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            
            if (inputStream != null) {
                val outputStream = FileOutputStream(destFile)
                
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                val resultPath = destFile.absolutePath
                Log.d("ImageUtils", "Successfully copied to: $resultPath")
                return resultPath
            } else {
                Log.e("ImageUtils", "Could not open InputStream for URI: $sourceUri")
                return null
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error copying URI to internal storage: ${e.message}", e)
            null
        }
    }
    

    fun deleteInternalStorageFile(filePath: String?) {
        if (filePath.isNullOrEmpty()) return
        
        try {
            val file = File(filePath)
            if (file.exists()) {
                val deleted = file.delete()
                Log.d("ImageUtils", "Deleted file $filePath: $deleted")
            }
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error deleting file $filePath: ${e.message}")
        }
    }
    

    fun filePathToUri(filePath: String?): Uri? {
        return if (!filePath.isNullOrEmpty()) {
            if (filePath.startsWith("content://")) {
                Log.w("ImageUtils", "Content URI detected, returning null due to permission issues: $filePath")
                return null
            }
            
            val file = File(filePath)
            if (file.exists()) {
                Uri.fromFile(file)
            } else {
                Log.w("ImageUtils", "File does not exist: $filePath")
                null
            }
        } else null
    }
} 