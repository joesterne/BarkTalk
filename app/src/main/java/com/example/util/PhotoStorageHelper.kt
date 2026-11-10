package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Utility to persist user-selected dog photos securely into app-internal storage.
 */
object PhotoStorageHelper {
    private const val TAG = "PhotoStorageHelper"

    fun savePhotoLocally(context: Context, sourceUri: Uri): String? {
        return try {
            val photosDir = File(context.filesDir, "pet_photos").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(photosDir, "pet_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy photo locally", e)
            sourceUri.toString()
        }
    }
}
