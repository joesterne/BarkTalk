package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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

@Entity(tableName = "translation_logs")
data class TranslationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val petId: Long,
    val petName: String,
    val petBreed: String,
    val timestamp: Long = System.currentTimeMillis(),
    val vocalizationType: String, // Single Bark, Rapid Alarm Bark, Play Growl, Whine, Howl, Yip, Panting
    val dominantPitchHz: Int,
    val intensityDb: Int,
    val cadenceDescription: String,
    val englishTranslation: String,
    val dogThought: String,
    val detectedEmotion: String,
    val urgencyLevel: String, // LOW, MEDIUM, HIGH, CRITICAL
    val confidencePercent: Int,
    val recommendedAction: String,
    val canineSubtext: String,
    val isFavorite: Boolean = false,
    val audioFilePath: String? = null
)
