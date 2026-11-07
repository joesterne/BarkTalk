package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity to store pet profiles.
 */
@Entity(tableName = "pet_profiles")
data class PetProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val breed: String,
    val ageYears: Int,
    val ageMonths: Int,
    val avatarEmoji: String = "🐶",
    val personality: String = "Gentle Goofball", // Excited, Loyal Guardian, Gentle Goofball, Dramatic Diva, Foodie, Anxious Sweetheart
    val pitchProfile: String = "Standard Bark", // High Yip, Standard Bark, Deep Woof
    val favoriteTreat: String = "Peanut Butter",
    val isSelected: Boolean = false
)

/**
 * Room Database Entity to store past canine translations.
 * Stores the timestamp, original audio intensity (dB), translated English text,
 * dominant acoustic pitch, detected emotion, inner dog thought, and optional audio recording path
 * so users can view and review their dog's communication history.
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
    val audioFilePath: String? = null
)

/**
 * Semantic type alias for TranslationLog representing a Dog Translation Room Entity.
 */
typealias DogTranslationEntity = TranslationLog
