package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.TranslationState
import com.example.audio.AudioProcessor
import com.example.data.TranslationLog
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.ShareTranslationDialog
import com.example.util.SocialShareHelper

@Composable
fun TranslateScreen(
    viewModel: BarkTalkViewModel,
    onNavigateToProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedPet by viewModel.selectedPet.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val liveDb by viewModel.liveVolumeDb.collectAsState()
    val livePitch by viewModel.livePitchHz.collectAsState()
    val waveform by viewModel.liveWaveform.collectAsState()
    val translationState by viewModel.translationState.collectAsState()
    val autoSpeak by viewModel.autoSpeakTranslations.collectAsState()
    val notifyUrgent by viewModel.notifyHighUrgency.collectAsState()

    // MediaRecorder states for audio sample capture
    val isMediaRecording by viewModel.isMediaRecording.collectAsState()
    val mediaDuration by viewModel.mediaRecordingDurationSeconds.collectAsState()
    val mediaDb by viewModel.mediaRecorderVolumeDb.collectAsState()
    val mediaWaveform by viewModel.mediaRecorderWaveform.collectAsState()
    val lastRecordedSample by viewModel.lastRecordedSample.collectAsState()
    val currentlyPlayingBarkPath by viewModel.currentlyPlayingBarkPath.collectAsState()

    var captureMode by remember { mutableStateOf("MediaRecorder") } // "MediaRecorder" or "ContinuousRadar"
    var shareDialogLog by remember { mutableStateOf<TranslationLog?>(null) }

    // Permission launcher for microphone (continuous radar)
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    // Permission launcher for MediaRecorder bark sample capture
    val mediaRecorderPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startMediaRecording()
        }
    }

    // Permission launcher for notifications on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.sendTestNotification()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("translate_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Card with Pet Info
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_pet_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(52.dp)
                            ) {
                                if (!selectedPet?.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = selectedPet?.photoUri,
                                        contentDescription = "${selectedPet?.name}'s photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.clip(CircleShape)
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = selectedPet?.avatarEmoji ?: "🐶",
                                            fontSize = 28.sp
                                        )
                                    }
                                }
                            }
                            Column {
                                Text(
                                    text = selectedPet?.name ?: "Barnaby",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${selectedPet?.breed ?: "Golden Retriever"} • ${selectedPet?.personality ?: "Gentle Goofball"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onNavigateToProfiles,
                            modifier = Modifier.testTag("switch_pet_button"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Switch", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 2. Microphone & Audio Capture Visualizer (MediaRecorder & Continuous Radar)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_visualizer_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mode Selector Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (captureMode == "MediaRecorder") MaterialTheme.colorScheme.primary else Color.Transparent,
                                    modifier = Modifier
                                        .clickable {
                                            if (isListening) viewModel.stopListening()
                                            captureMode = "MediaRecorder"
                                        }
                                        .testTag("mode_media_recorder")
                                ) {
                                    Text(
                                        text = "🎙️ MediaRecorder Sample",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (captureMode == "MediaRecorder") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (captureMode == "ContinuousRadar") MaterialTheme.colorScheme.secondary else Color.Transparent,
                                    modifier = Modifier
                                        .clickable {
                                            if (isMediaRecording) viewModel.stopMediaRecording()
                                            captureMode = "ContinuousRadar"
                                        }
                                        .testTag("mode_continuous_radar")
                                ) {
                                    Text(
                                        text = "📡 Ambient Radar",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (captureMode == "ContinuousRadar") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = if (captureMode == "MediaRecorder") {
                            if (isMediaRecording) "🔴 Recording Dog Bark via MediaRecorder..." else "Capture Dog Bark (MediaRecorder)"
                        } else {
                            if (isListening) "Listening for Barks & Whines..." else "Real-Time Ambient Dog Audio Radar"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isMediaRecording || isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(12.dp))

                    // Live Waveform Canvas (MediaRecorder or AudioRecord)
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val activeWaveform = if (captureMode == "MediaRecorder") mediaWaveform else waveform
                    val isActivelyCapturing = isMediaRecording || isListening

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                            .testTag("waveform_canvas")
                    ) {
                        val barCount = activeWaveform.size
                        val barWidth = (size.width / (barCount * 1.5f))
                        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
                        val centerY = size.height / 2f

                        for (i in 0 until barCount) {
                            val magnitude = if (isActivelyCapturing) {
                                (activeWaveform[i] * (size.height / 2.2f)).coerceAtLeast(4f)
                            } else {
                                6f
                            }
                            val x = i * (barWidth + spacing)
                            drawRoundRect(
                                color = if (isActivelyCapturing && activeWaveform[i] > 0.35f) primaryColor else secondaryColor.copy(alpha = 0.7f),
                                topLeft = Offset(x, centerY - magnitude),
                                size = Size(barWidth, magnitude * 2f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Live Acoustic Metrics Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (captureMode == "MediaRecorder") {
                                        if (isMediaRecording) String.format("⏱️ %.1f s", mediaDuration) else "⏱️ 0.0 s (Timer)"
                                    } else {
                                        if (isListening && livePitch > 0) "$livePitch Hz (Pitch)" else "-- Hz (Pitch)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔊",
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (captureMode == "MediaRecorder") {
                                        if (isMediaRecording) "${mediaDb.toInt()} dB (Peak)" else "-- dB (Peak)"
                                    } else {
                                        if (isListening) "${liveDb.toInt()} dB (Level)" else "-- dB (Level)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Pulsing Microphone Button
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = if (isActivelyCapturing) 1.15f else 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "mic_scale"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(92.dp)
                            .scale(pulseScale)
                    ) {
                        if (isActivelyCapturing) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isActivelyCapturing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable {
                                    if (captureMode == "MediaRecorder") {
                                        if (isMediaRecording) {
                                            viewModel.stopMediaRecording()
                                        } else {
                                            mediaRecorderPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    } else {
                                        if (isListening) {
                                            viewModel.stopListening()
                                        } else {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                }
                                .testTag(if (captureMode == "MediaRecorder") "media_recorder_mic_button" else "record_mic_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isActivelyCapturing) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = if (isActivelyCapturing) "Stop Recording" else "Record Bark",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (captureMode == "MediaRecorder") {
                            if (isMediaRecording) "Tap to stop & translate dog bark audio sample" else "Tap to record dog bark sample with MediaRecorder"
                        } else {
                            if (isListening) "Tap to pause ambient audio capture" else "Tap microphone for continuous ambient listening"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Quick Bark Simulator Chips (Test without real barking dog)
        item {
            Column {
                Text(
                    text = "Instant Bark Simulator (Try Different Vocalizations):",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("simulator_chips_row")
                ) {
                    items(AudioProcessor.SAMPLE_SCENARIOS) { scenario ->
                        SuggestionChip(
                            onClick = {
                                viewModel.simulateBarkScenario(scenario)
                            },
                            label = {
                                Text(
                                    text = when (scenario.vocalizationType) {
                                        "Rapid Alarm Bark" -> "🚨 Doorbell Bark"
                                        "High Whine & Whimper" -> "🥺 Storm Whine"
                                        "Playful Yip & Chuff" -> "🎾 Play Yip"
                                        "Low Rumble Play Growl" -> "🦁 Tug Growl"
                                        "Midnight Melodic Howl" -> "🐺 Midnight Howl"
                                        else -> "🍖 Dinner Whimper"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.testTag("chip_${scenario.vocalizationType}")
                        )
                    }
                }
            }
        }

        // 4. Translation State (Translating / Success / Error)
        item {
            when (val state = translationState) {
                is TranslationState.TranslatingWithAi -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translating_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Column {
                                Text(
                                    text = "Gemini AI Interpreting Acoustics...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Analyzing pitch harmonics, vocal timbre & breed temperament...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                is TranslationState.Success -> {
                    val result = state.result
                    val analysis = state.analysis
                    val urgencyColor = when (result.urgencyLevel) {
                        "CRITICAL" -> MaterialTheme.colorScheme.error
                        "HIGH" -> MaterialTheme.colorScheme.tertiary
                        "MEDIUM" -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translation_result_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Top badge: Vocalization type + Urgency pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = analysis.vocalizationType,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = urgencyColor.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, urgencyColor)
                                ) {
                                    Text(
                                        text = "🚨 Urgency: ${result.urgencyLevel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = urgencyColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // The Big English Translation
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "🗣️ English Translation:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = "\"${result.englishTranslation}\"",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Dog's Inner Monologue Thought
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("💭", fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Canine Inner Thought:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = result.dogThought,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Emotion Pill & Confidence
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "❤️ Emotion: ${result.detectedEmotion}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "🎯 Confidence: ${result.confidencePercent}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            // Recommended Action Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "💡 Advice for Human:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = result.recommendedAction,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            // Scientific Acoustic Reasoning Subtext
                            Text(
                                text = "🔬 Acoustic Analysis: ${result.canineSubtext} (${analysis.pitchHz}Hz • ${analysis.volumeDb}dB)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )

                            // Replay recorded dog bark audio if captured via MediaRecorder
                            val recordedSample = lastRecordedSample
                            if (recordedSample != null && recordedSample.audioFile.exists()) {
                                val isPlayingBark = currentlyPlayingBarkPath == recordedSample.audioFile.absolutePath
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("🎙️", fontSize = 18.sp)
                                                }
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Captured Bark Audio (${String.format("%.1f", recordedSample.durationSeconds)}s)",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                                )
                                                Text(
                                                    text = "MediaRecorder AAC • ${recordedSample.peakDb} dB Peak",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (isPlayingBark) {
                                                    viewModel.stopPlayingRecordedBark()
                                                } else {
                                                    viewModel.playRecordedBark(recordedSample.audioFile.absolutePath)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isPlayingBark) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.testTag("play_recorded_bark_button")
                                        ) {
                                            Icon(
                                                if (isPlayingBark) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isPlayingBark) "Stop" else "Play Bark")
                                        }
                                    }
                                }
                            }

                            // Action Buttons: Speak (TTS) & Share
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.speakTranslation(result.englishTranslation)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("speak_translation_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Speak Voice")
                                }

                                Button(
                                    onClick = {
                                        val tempLog = TranslationLog(
                                            petId = selectedPet?.id ?: 1,
                                            petName = selectedPet?.name ?: "Barnaby",
                                            petBreed = selectedPet?.breed ?: "Dog",
                                            vocalizationType = analysis.vocalizationType,
                                            dominantPitchHz = analysis.pitchHz,
                                            intensityDb = analysis.volumeDb,
                                            cadenceDescription = analysis.cadenceDescription,
                                            englishTranslation = result.englishTranslation,
                                            dogThought = result.dogThought,
                                            detectedEmotion = result.detectedEmotion,
                                            urgencyLevel = result.urgencyLevel,
                                            confidencePercent = result.confidencePercent,
                                            recommendedAction = result.recommendedAction,
                                            canineSubtext = result.canineSubtext
                                        )
                                        shareDialogLog = tempLog
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_translation_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }

                is TranslationState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translation_error_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Translation Notice",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.resetTranslationState() },
                                modifier = Modifier.testTag("dismiss_error_button")
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                else -> {}
            }
        }

        // 5. App Controls & Notification Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Translation & Alert Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Speak Translations", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Reads translated canine speech aloud in English", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = autoSpeak,
                            onCheckedChange = { viewModel.toggleAutoSpeak() },
                            modifier = Modifier.testTag("auto_speak_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Push Alerts for Urgent Barks", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("Sends notification when high urgency vocalization detected", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = notifyUrgent,
                            onCheckedChange = { viewModel.toggleNotifyHighUrgency() },
                            modifier = Modifier.testTag("urgent_alerts_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.sendTestNotification()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_notification_button")
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Send Test Urgent Alert Notification")
                    }
                }
            }
        }

        // 6. Home Screen Widget Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("widget_info_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Home Screen Dog Mood Widget",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = "Add the BarkTalk widget to your Android home screen to see ${selectedPet?.name ?: "your dog"}'s real-time mood indicator and recent translation snippet at a glance! Touch and hold your home screen > Widgets > BarkTalk.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    OutlinedButton(
                        onClick = {
                            com.example.widget.BarkTalkWidgetProvider.updateAllWidgets(context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_widget_button")
                    ) {
                        Text("🔄 Force Sync Home Screen Widget")
                    }
                }
            }
        }
    }

    shareDialogLog?.let { logToShare ->
        ShareTranslationDialog(
            log = logToShare,
            onDismissRequest = { shareDialogLog = null }
        )
    }
}

