package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity to store recorded dog barks, including a timestamp,
 * the AI-generated translation, acoustic telemetry, and optional audio file path.
 */
@Entity(tableName = "recorded_barks")
data class RecordedBark(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val translation: String, // AI-generated English translation of the recorded dog bark
    val audioFilePath: String? = null,
    val durationSeconds: Float = 1.5f,
    val peakDb: Int = 75,
    val dominantPitchHz: Int = 500,
    val vocalizationType: String = "Single Bark",
    val cadenceDescription: String = "Standard cadence",
    val detectedEmotion: String = "Alert",
    val dogThought: String = "",
    val urgencyLevel: String = "LOW",
    val confidencePercent: Int = 92,
    val petId: Long = 1L,
    val petName: String = "Barnaby",
    val petBreed: String = "Golden Retriever",
    val isFavorite: Boolean = false
) {
    /**
     * Convenience accessor for the AI-generated translation.
     */
    val aiTranslation: String
        get() = translation

    /**
     * Convenience accessor matching TranslationLog naming convention.
     */
    val englishTranslation: String
        get() = translation
}

typealias BarkRecord = RecordedBark
typealias DogBarkEntity = RecordedBark
