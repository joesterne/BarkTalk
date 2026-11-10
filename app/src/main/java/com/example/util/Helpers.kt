package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.TranslationLog
import java.util.Locale

class NotificationHelper(private val context: Context) {
    private val channelId = "barktalk_alerts_channel"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Dog Translation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High urgency alerts and important bark translations from your dog"
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUrgentBarkAlert(log: TranslationLog) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            log.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = when (log.urgencyLevel) {
            "CRITICAL" -> "🚨"
            "HIGH" -> "⚠️"
            else -> "🐶"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("$emoji ${log.petName}: Urgent Bark Alert!")
            .setContentText("\"${log.englishTranslation}\"")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("\"${log.englishTranslation}\"\n\n🚨 Urgency: ${log.urgencyLevel}\n💡 Suggested Action: ${log.recommendedAction}\n📊 Acoustics: ${log.dominantPitchHz}Hz • ${log.vocalizationType}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(log.id.toInt().coerceAtLeast(101), builder.build())
    }

    fun sendTestAlert(petName: String) {
        val testLog = TranslationLog(
            id = 999,
            petId = 1,
            petName = petName,
            petBreed = "Canine",
            timestamp = System.currentTimeMillis(),
            vocalizationType = "Alarm Bark",
            dominantPitchHz = 520,
            intensityDb = 85,
            cadenceDescription = "Repeated rapid bursts",
            englishTranslation = "Someone is at the front gate! Check immediately!",
            dogThought = "I am protecting our perimeter!",
            detectedEmotion = "Intruder Alert",
            urgencyLevel = "HIGH",
            confidencePercent = 96,
            recommendedAction = "Check front door or yard.",
            canineSubtext = "High pitch alarm bark pattern."
        )
        showUrgentBarkAlert(testLog)
    }
}


class CanineTtsPlayer(private val context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "CanineTtsPlayer"
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            tts?.setPitch(1.15f) // slightly bright, expressive pitch
            tts?.setSpeechRate(1.05f)
            isInitialized = true
        } else {
            Log.e(TAG, "TextToSpeech init failed with status: $status")
        }
    }

    fun speak(text: String) {
        if (isInitialized && text.isNotBlank()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "barktalk_tts_${System.currentTimeMillis()}")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}
