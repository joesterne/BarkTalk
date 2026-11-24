package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity to store pet profiles.
 * Includes the dog's name, breed, age, and photo for local Room database persistence.
 */
@Entity(tableName = "pet_profiles")
data class PetProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val breed: String,
    val age: Int = 3, // Dog's age in years
    val ageYears: Int = age,
    val ageMonths: Int = 0,
    val photoUri: String? = null, // Path or URI to dog's photo
    val avatarEmoji: String = "🐶",
    val personality: String = "Gentle Goofball", // Excited, Loyal Guardian, Gentle Goofball, Dramatic Diva, Foodie, Anxious Sweetheart
    val pitchProfile: String = "Standard Bark", // High Yip, Standard Bark, Deep Woof
    val favoriteTreat: String = "Peanut Butter",
    val isSelected: Boolean = false
)

/**
 * Room Database Entity to store past canine bark translation logs.
 * Stores the translated English text, timestamp, optional user-assigned dog tag,
 * acoustic telemetry (pitch, dB, cadence), detected emotion, inner dog thought,
 * and optional audio recording path in the local Room database.
 */
@Entity(tableName = "translation_logs")
data class TranslationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val petName: String,
    val petBreed: String,
    val timestamp: Long = System.currentTimeMillis(),
    val vocalizationType: String = "Single Bark", // Single Bark, Rapid Alarm Bark, Play Growl, Whine, Howl, Yip, Panting
    val dominantPitchHz: Int = 500,
    val intensityDb: Int = 75, // Original audio intensity in decibels
    val cadenceDescription: String = "Standard cadence",
    val englishTranslation: String, // Translated English text for human companion
    val dogThought: String = "",
    val detectedEmotion: String = "Neutral",
    val urgencyLevel: String = "LOW", // LOW, MEDIUM, HIGH, CRITICAL
    val confidencePercent: Int = 90,
    val recommendedAction: String = "",
    val canineSubtext: String = "",
    val isFavorite: Boolean = false,
    val audioFilePath: String? = null,
    val dogTag: String? = null // Optional user-assigned dog tag (e.g. "Squirrel Patrol", "Treat Time")
) {
    /**
     * Convenience accessor for the translated bark text stored in Room.
     */
    val translatedText: String
        get() = englishTranslation
}

/**
 * Semantic type alias for TranslationLog representing a Dog Translation Room Entity.
 */
typealias DogTranslationEntity = TranslationLog
