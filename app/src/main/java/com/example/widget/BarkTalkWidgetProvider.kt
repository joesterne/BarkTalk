package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.PetProfile
import com.example.data.TranslationLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BarkTalkWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET || intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BarkTalkWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            updateWidgetsInternal(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateWidgetsInternal(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val selectedPet = database.petProfileDao().getSelectedProfile().firstOrNull()
                    ?: database.petProfileDao().getAllProfiles().firstOrNull()?.firstOrNull()
                    ?: PetProfile(name = "Barnaby", breed = "Golden Retriever", ageYears = 3, ageMonths = 4)

                val latestLogs = database.translationLogDao().getAllLogs().firstOrNull()
                val latestLog = latestLogs?.firstOrNull()

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_dog_translator)
                    bindWidgetViews(context, views, selectedPet, latestLog)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed updating dog translator widgets", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun bindWidgetViews(
        context: Context,
        views: RemoteViews,
        pet: PetProfile,
        log: TranslationLog?
    ) {
        // 1. Dog identity
        views.setTextViewText(R.id.widget_pet_avatar, pet.avatarEmoji)
        views.setTextViewText(R.id.widget_pet_name, pet.name)
        views.setTextViewText(R.id.widget_pet_subtitle, "${pet.breed} • ${pet.personality}")

        // 2. Real-time Mood Indicator based on latest translations & acoustics
        val moodText = calculateMoodIndicator(log)
        views.setTextViewText(R.id.widget_mood_badge, moodText)

        // 3. Message snippet & dog thought
        if (log != null) {
            views.setTextViewText(R.id.widget_translation_quote, "\"${log.englishTranslation}\"")
            val timeAgo = formatTimeAgo(log.timestamp)
            views.setTextViewText(R.id.widget_thought_snippet, "💭 ${log.dogThought} • $timeAgo")
            views.setTextViewText(
                R.id.widget_acoustics_badge,
                "📊 ${log.dominantPitchHz} Hz • ${log.vocalizationType}"
            )
        } else {
            views.setTextViewText(
                R.id.widget_translation_quote,
                "\"Woof! Tap 'Listen' to translate my first bark!\""
            )
            views.setTextViewText(
                R.id.widget_thought_snippet,
                "💭 Ready to listen & interpret canine sounds"
            )
            views.setTextViewText(
                R.id.widget_acoustics_badge,
                "📊 Awaiting canine audio"
            )
        }

        // 4. Click Intent: Root Widget opens app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        // 5. Click Intent: "🎤 Listen" launches app with immediate microphone capture
        val listenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_START_LISTENING, true)
        }
        val listenPendingIntent = PendingIntent.getActivity(
            context,
            1,
            listenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_translate, listenPendingIntent)

        // 6. Click Intent: "🔊 Talk Back" opens soundboard directly
        val soundboardIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_START_TAB, "soundboard")
        }
        val soundboardPendingIntent = PendingIntent.getActivity(
            context,
            2,
            soundboardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_soundboard, soundboardPendingIntent)

        // 7. Click Intent: Refresh button triggers immediate widget reload
        val refreshIntent = Intent(context, BarkTalkWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            3,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)
    }

    companion object {
        private const val TAG = "BarkTalkWidget"
        const val ACTION_REFRESH_WIDGET = "com.example.widget.ACTION_REFRESH_WIDGET"
        const val EXTRA_START_LISTENING = "extra_start_listening"
        const val EXTRA_START_TAB = "extra_start_tab"

        fun calculateMoodIndicator(log: TranslationLog?): String {
            if (log == null) return "🐾 Calm & Ready"

            val emotionLower = log.detectedEmotion.lowercase()
            val urgency = log.urgencyLevel.uppercase()

            return when {
                urgency == "CRITICAL" -> "🚨 Urgent Alert"
                urgency == "HIGH" || emotionLower.contains("alert") || emotionLower.contains("vigilant") -> "⚠️ Guarding Alert"
                emotionLower.contains("joy") || emotionLower.contains("play") || emotionLower.contains("happy") -> "🎾 Playful & Happy"
                emotionLower.contains("food") || emotionLower.contains("anticipation") || emotionLower.contains("treat") -> "🍖 Hungry & Hopeful"
                emotionLower.contains("anxious") || emotionLower.contains("fear") || emotionLower.contains("whine") -> "🥺 Seeking Comfort"
                emotionLower.contains("howl") || emotionLower.contains("pack") -> "🐺 Calling Pack"
                emotionLower.contains("love") || emotionLower.contains("affection") -> "❤️ Full of Love"
                else -> "🐶 ${log.detectedEmotion}"
            }
        }

        fun formatTimeAgo(timestamp: Long): String {
            val diffMs = System.currentTimeMillis() - timestamp
            val minutes = diffMs / (1000 * 60)
            return when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "${minutes}m ago"
                minutes < 1440 -> "${minutes / 60}h ago"
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
            }
        }

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, BarkTalkWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
