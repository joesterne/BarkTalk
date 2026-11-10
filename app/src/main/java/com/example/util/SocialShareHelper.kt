package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.example.data.TranslationLog

/**
 * Supported text snippet formats for social and messaging export.
 */
enum class ShareFormat(val label: String, val iconEmoji: String, val description: String) {
    CONCISE(
        label = "Quick Text",
        iconEmoji = "💬",
        description = "Short & sweet quote for instant texting"
    ),
    DETAILED(
        label = "Full Card",
        iconEmoji = "📋",
        description = "Complete translation with emotions & advice"
    ),
    STORY_POST(
        label = "Dog Story",
        iconEmoji = "🐶",
        description = "Playful comedic monologue from your dog"
    ),
    ACOUSTIC_REPORT(
        label = "Acoustic Data",
        iconEmoji = "📊",
        description = "Technical telemetry with pitch & decibel levels"
    )
}

/**
 * Social Sharing Helper utilizing Android's ShareSheet intent.
 * Exports canine vocalization translations as formatted text snippets to messaging apps.
 */
object SocialShareHelper {

    /**
     * Builds a formatted text snippet from a TranslationLog based on the chosen ShareFormat.
     */
    fun buildShareSnippet(log: TranslationLog, format: ShareFormat = ShareFormat.CONCISE): String {
        return when (format) {
            ShareFormat.CONCISE -> buildString {
                appendLine("🐶 ${log.petName} (${log.petBreed}):")
                appendLine("🗣️ \"${log.englishTranslation}\"")
                if (log.dogThought.isNotBlank()) {
                    appendLine("💭 (Inner thought: ${log.dogThought})")
                }
                appendLine()
                append("🐾 Translated live via BarkTalk")
            }

            ShareFormat.DETAILED -> buildString {
                appendLine("🐾 BarkTalk Canine Translation 🐾")
                appendLine("🐶 ${log.petName} • ${log.petBreed}")
                appendLine()
                appendLine("🗣️ \"${log.englishTranslation}\"")
                appendLine()
                if (log.dogThought.isNotBlank()) {
                    appendLine("💭 Dog Thought: ${log.dogThought}")
                }
                appendLine("❤️ Detected Emotion: ${log.detectedEmotion}")
                appendLine("⚡ Urgency: ${log.urgencyLevel} (${log.confidencePercent}% confidence)")
                appendLine("📊 Acoustic Profile: ${log.dominantPitchHz} Hz • ${log.vocalizationType} (${log.intensityDb} dB)")
                if (log.recommendedAction.isNotBlank()) {
                    appendLine("💡 Suggested Action: ${log.recommendedAction}")
                }
                appendLine()
                append("#BarkTalk #DogTranslator #CanineAI #PetLife")
            }

            ShareFormat.STORY_POST -> buildString {
                appendLine("🚨 DISPATCH FROM MY DOG'S BRAIN 🚨")
                appendLine("🐶 Subject: ${log.petName} (${log.petBreed})")
                appendLine("📢 Official Statement:")
                appendLine("\"${log.englishTranslation}\"")
                appendLine()
                if (log.dogThought.isNotBlank()) {
                    appendLine("🧠 Unfiltered Internal Monologue:")
                    appendLine("\"${log.dogThought}\"")
                    appendLine()
                }
                appendLine("📈 Vocal Intensity: ${log.intensityDb} dB of pure canine emotion")
                appendLine("🐕 Detected Mood: ${log.detectedEmotion}")
                appendLine()
                append("🐾 Decoded in real-time with BarkTalk App")
            }

            ShareFormat.ACOUSTIC_REPORT -> buildString {
                appendLine("📋 Canine Acoustic Telemetry Report")
                appendLine("🐶 Subject: ${log.petName} (${log.petBreed})")
                appendLine("🔊 Vocalization Type: ${log.vocalizationType}")
                appendLine("🎵 Dominant Pitch: ${log.dominantPitchHz} Hz")
                appendLine("📈 Sound Intensity: ${log.intensityDb} dB")
                appendLine("⏱️ Cadence: ${log.cadenceDescription}")
                appendLine("⚡ Urgency: ${log.urgencyLevel}")
                appendLine("🗣️ English Translation: \"${log.englishTranslation}\"")
                if (log.canineSubtext.isNotBlank()) {
                    appendLine("🔬 Ethological Analysis: ${log.canineSubtext}")
                }
                appendLine()
                append("🐾 BarkTalk Acoustic Telemetry Engine")
            }
        }
    }

    /**
     * Launches Android's native ShareSheet Intent (Intent.ACTION_SEND + Intent.createChooser)
     * allowing the user to select any messaging or social app (SMS, WhatsApp, Telegram, etc.).
     */
    fun shareTranslation(
        context: Context,
        log: TranslationLog,
        format: ShareFormat = ShareFormat.CONCISE
    ) {
        val snippetText = buildShareSnippet(log, format)
        val subjectTitle = "🐶 ${log.petName}'s Dog Translation"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subjectTitle)
            putExtra(Intent.EXTRA_TEXT, snippetText)
        }

        val chooserTitle = "Share ${log.petName}'s Translation to..."
        val chooserIntent = Intent.createChooser(sendIntent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                putExtra(Intent.EXTRA_TITLE, subjectTitle)
            }
        }

        try {
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "No messaging app found to share snippet", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the formatted snippet to the device clipboard with instant feedback.
     */
    fun copyToClipboard(
        context: Context,
        text: String,
        label: String = "BarkTalk Dog Translation"
    ): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Translation copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Failed copying to clipboard", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
