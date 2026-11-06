package com.example.ai

import android.util.Log
import com.example.audio.AcousticAnalysis
import com.example.data.PetProfile
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

data class DogTranslationResult(
    val englishTranslation: String,
    val dogThought: String,
    val detectedEmotion: String,
    val urgencyLevel: String,
    val confidencePercent: Int,
    val recommendedAction: String,
    val canineSubtext: String
)

sealed interface TranslationState {
    data object Idle : TranslationState
    data object AnalyzingAcoustics : TranslationState
    data object TranslatingWithAi : TranslationState
    data class Success(val result: DogTranslationResult, val analysis: AcousticAnalysis) : TranslationState
    data class Error(val message: String) : TranslationState
}

class DogAiTranslator {
    private val TAG = "DogAiTranslator"

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "englishTranslation" to Schema.string(
                description = "The dog's speech translated directly into vivid, emotional, natural English from the dog's perspective."
            ),
            "dogThought" to Schema.string(
                description = "The inner monologue or humorous stream of consciousness of the dog."
            ),
            "detectedEmotion" to Schema.string(
                description = "The primary canine emotion (e.g., Alert & Protective, Pure Joy & Play, Food Anticipation, Separation Anxiety, Affectionate Love, Frustrated Want)."
            ),
            "urgency" to Schema.string(
                description = "Urgency category: LOW, MEDIUM, HIGH, or CRITICAL."
            ),
            "confidence" to Schema.integer(
                description = "Confidence percentage between 75 and 99."
            ),
            "recommendedAction" to Schema.string(
                description = "Actionable, helpful advice for the human owner on how to respond."
            ),
            "canineSubtext" to Schema.string(
                description = "Acoustic reasoning connecting the pitch, cadence, and breed traits to this canine translation."
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    suspend fun translateCanineVocalization(
        pet: PetProfile,
        acousticData: AcousticAnalysis
    ): DogTranslationResult {
        val prompt = buildString {
            appendLine("You are an expert canine ethologist and dog vocalization translator.")
            appendLine("Translate what this specific dog is communicating based on acoustic telemetry and personality profile:")
            appendLine()
            appendLine("DOG PROFILE:")
            appendLine("- Name: ${pet.name}")
            appendLine("- Breed: ${pet.breed}")
            appendLine("- Age: ${pet.ageYears} years, ${pet.ageMonths} months")
            appendLine("- Personality: ${pet.personality}")
            appendLine("- Typical Pitch Category: ${pet.pitchProfile}")
            appendLine("- Favorite Treat: ${pet.favoriteTreat}")
            appendLine()
            appendLine("ACOUSTIC TELEMETRY:")
            appendLine("- Classified Vocalization: ${acousticData.vocalizationType}")
            appendLine("- Dominant Pitch: ${acousticData.pitchHz} Hz")
            appendLine("- Sound Intensity: ${acousticData.volumeDb} dB")
            appendLine("- Cadence & Rhythm: ${acousticData.cadenceDescription}")
            appendLine()
            appendLine("INSTRUCTIONS:")
            appendLine("1. Generate an authentic, expressive English translation capturing what ${pet.name} is thinking and shouting.")
            appendLine("2. Include ${pet.name}'s specific breed instincts (e.g. Golden retriever wanting to fetch/please, Frenchie drama/snorting, German Shepherd guard vigilance).")
            appendLine("3. Provide an insightful inner thought and clear owner recommendation.")
            appendLine("4. Urgency must be one of: LOW, MEDIUM, HIGH, or CRITICAL.")
        }

        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val jsonText = response.text ?: throw IllegalStateException("Model returned empty response.")
            val jsonObj = JSONObject(jsonText)

            DogTranslationResult(
                englishTranslation = jsonObj.optString("englishTranslation", "Woof! (I have something important to say!)"),
                dogThought = jsonObj.optString("dogThought", "Looking at my human with intense love and excitement."),
                detectedEmotion = jsonObj.optString("detectedEmotion", "Alert & Happy"),
                urgencyLevel = jsonObj.optString("urgency", "MEDIUM").uppercase(),
                confidencePercent = jsonObj.optInt("confidence", 92).coerceIn(70, 99),
                recommendedAction = jsonObj.optString("recommendedAction", "Give attention, check surroundings, or offer a favorite treat."),
                canineSubtext = jsonObj.optString("canineSubtext", "Acoustics show typical tonal cadence for ${pet.breed}.")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini translation timed out after 60s", e)
            throw IllegalStateException("Translation timed out. Please check connection and try again.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini translation failed", e)
            throw e
        }
    }
}
