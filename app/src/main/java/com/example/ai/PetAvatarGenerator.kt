package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.example.data.PetProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.File
import java.io.FileOutputStream

/**
 * UI state representing Gemini AI digital avatar generation for a pet profile.
 */
sealed interface AvatarGenerationState {
    data object Idle : AvatarGenerationState
    data class Generating(val petId: Long?, val petName: String) : AvatarGenerationState
    data class Success(
        val petId: Long?,
        val avatarUri: String,
        val bitmap: Bitmap,
        val petName: String
    ) : AvatarGenerationState
    data class Error(val petId: Long?, val message: String) : AvatarGenerationState
}

/**
 * Generates unique stylized digital pet avatars using Firebase AI Logic
 * (`gemini-nano-banana-2.1`) tailored to a dog's breed, age, and personality traits.
 */
class PetAvatarGenerator {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseModalities = listOf(ResponseModality.IMAGE)
            }
        )
    }

    suspend fun generateDigitalAvatarBitmap(pet: PetProfile): Bitmap {
        val prompt = buildAvatarPrompt(pet)
        return try {
            val response = withTimeout(AVATAR_TIMEOUT_MS) {
                model.generateContent(prompt)
            }
            val imagePart = response.candidates.firstOrNull()
                ?.content
                ?.parts
                ?.filterIsInstance<ImagePart>()
                ?.firstOrNull()

            imagePart?.image
                ?: throw IllegalStateException("Gemini did not return an avatar image for ${pet.name.ifBlank { "this dog" }}.")
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini avatar generation timed out after ${AVATAR_TIMEOUT_MS}ms", e)
            throw IllegalStateException("Avatar generation timed out. Please check your connection and try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini avatar generation failed", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "PetAvatarGenerator"
        const val MODEL_NAME = "gemini-nano-banana-2.1"
        const val AVATAR_TIMEOUT_MS = 60_000L
        private const val AVATAR_DIR_NAME = "pet_avatars"

        /**
         * Builds a rich image-generation prompt tailored to the dog's breed, age,
         * personality archetype, and favorite treat.
         */
        fun buildAvatarPrompt(pet: PetProfile): String {
            val dogName = pet.name.trim().ifBlank { "Good Dog" }
            val breed = pet.breed.trim().ifBlank { "Mixed Breed Dog" }
            val ageYears = pet.ageYears.coerceAtLeast(0)
            val lifeStage = when {
                ageYears <= 1 -> "energetic young puppy with soft oversized paws and curious wide eyes"
                ageYears >= 8 -> "distinguished senior dog with gentle soulful eyes and subtle frosted muzzle"
                else -> "vibrant adult dog in healthy prime"
            }

            val personalityVibe = when (pet.personality.trim()) {
                "Gentle Goofball" -> "playful happy head tilt, bouncy floppy ears, joyful tongue-out grin, whimsical warm sparkles"
                "Loyal Guardian" -> "noble confident chest-up posture, alert upright gaze, sleek hero collar tag, warm protective aura"
                "Excited Zoomer" -> "huge radiant open-mouth smile, wind-swept ears, sparkling dynamic energy, lively playful expression"
                "Dramatic Diva" -> "expressive raised eyebrows, charming chic bandana accessory, glamorous poised expression"
                "Foodie Connoisseur" -> "delighted bright eyes, eager happy smile, wearing a tiny playful neckerchief"
                "Anxious Sweetheart" -> "tender soulful puppy eyes, cozy soft knit scarf, comforting gentle pastel glow"
                else -> "expressive friendly eyes, charming canine personality (${pet.personality})"
            }

            val treatAccent = pet.favoriteTreat.trim().takeIf { it.isNotBlank() }?.let {
                "Subtle tiny stylized icon motif inspired by '$it' tucked cleanly onto the collar charm."
            } ?: ""

            return buildString {
                append("Create a charming, high-craft stylized 3D animated character digital avatar portrait of a $breed dog named $dogName ($lifeStage). ")
                append("Personality and expression: $personalityVibe. ")
                if (treatAccent.isNotBlank()) {
                    append(treatAccent)
                    append(" ")
                }
                append("Composition: centered bust portrait framed cleanly inside a soft circular-friendly vignette against a smooth warm complementary gradient backdrop. ")
                append("Art direction: polished modern Pixar-inspired stylized illustration with rich fur texture, expressive catchlights in the eyes, crisp studio rim lighting, vibrant inviting colors. ")
                append("Strictly no text, no letters, no labels, no frames, and no watermarks.")
            }
        }

        /**
         * Saves a generated avatar Bitmap into app-internal storage (`filesDir/pet_avatars/`)
         * and returns its `file://` URI string for Coil & Room persistence.
         */
        fun saveAvatarBitmapToFile(
            context: Context,
            bitmap: Bitmap,
            petName: String
        ): String {
            val avatarDir = File(context.filesDir, AVATAR_DIR_NAME).apply {
                if (!exists()) mkdirs()
            }
            val safeName = petName
                .lowercase()
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "pet" }
            val destinationFile = File(
                avatarDir,
                "avatar_${safeName}_${System.currentTimeMillis()}.png"
            )
            FileOutputStream(destinationFile).use { output ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 95, output)
                if (!compressed) {
                    throw IllegalStateException("Failed to compress generated avatar image.")
                }
            }
            return Uri.fromFile(destinationFile).toString()
        }

        fun isAiGeneratedAvatar(photoUri: String?): Boolean {
            return !photoUri.isNullOrBlank() && photoUri.contains(AVATAR_DIR_NAME)
        }
    }
}
