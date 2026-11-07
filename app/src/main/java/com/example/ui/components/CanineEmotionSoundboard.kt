package com.example.ui.components

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.CanineSoundboardPlayer
import com.example.audio.SoundboardItem
import kotlin.math.sin

/**
 * Canine Emotion Soundboard Component.
 * Maps canine emotions (Happy, Playful, Warning, Alert, Curious, Calm)
 * to synthesized acoustic barks played via the device speaker.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CanineEmotionSoundboard(
    items: List<SoundboardItem>,
    currentlyPlayingId: String?,
    onPlaySound: (SoundboardItem) -> Unit,
    onStopSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedEmotion by remember { mutableStateOf("All") }

    val emotions = listOf(
        EmotionFilter("All", "🌟", null),
        EmotionFilter("Happy", "🐶", Color(0xFFE65100)),
        EmotionFilter("Playful", "🎾", Color(0xFF00897B)),
        EmotionFilter("Warning", "⚠️", Color(0xFFD32F2F)),
        EmotionFilter("Alert", "🚨", Color(0xFFF57C00)),
        EmotionFilter("Curious", "🧐", Color(0xFF5E35B1)),
        EmotionFilter("Calm", "🧘", Color(0xFF2E7D32))
    )

    val filteredItems = remember(selectedEmotion, items) {
        if (selectedEmotion == "All") items
        else items.filter { it.emotion.equals(selectedEmotion, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("canine_emotion_soundboard")
    ) {
        // Quick Emotion Trigger Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Device Speaker Synthesizer",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Acoustic canine frequency synthesis",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (currentlyPlayingId != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.clickable { onStopSound() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "Stop",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Stop Audio",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Quick Emotion Instant Trigger Buttons
                Text(
                    text = "Quick Emotion Triggers:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickTriggerPill(
                        emoji = "🐶",
                        label = "Happy Bark",
                        color = Color(0xFFFFB300),
                        testTag = "quick_trigger_happy",
                        isPlaying = currentlyPlayingId == "happy_greeting",
                        onClick = {
                            items.find { it.id == "happy_greeting" }?.let(onPlaySound)
                        }
                    )

                    QuickTriggerPill(
                        emoji = "🎾",
                        label = "Play Bow",
                        color = Color(0xFF26A69A),
                        testTag = "quick_trigger_playful",
                        isPlaying = currentlyPlayingId == "playful_bow",
                        onClick = {
                            items.find { it.id == "playful_bow" }?.let(onPlaySound)
                        }
                    )

                    QuickTriggerPill(
                        emoji = "⚠️",
                        label = "Warning Bark",
                        color = Color(0xFFEF5350),
                        testTag = "quick_trigger_warning",
                        isPlaying = currentlyPlayingId == "warning_guard",
                        onClick = {
                            items.find { it.id == "warning_guard" }?.let(onPlaySound)
                        }
                    )

                    QuickTriggerPill(
                        emoji = "🚨",
                        label = "Sentinel Alert",
                        color = Color(0xFFFF9800),
                        testTag = "quick_trigger_alert",
                        isPlaying = currentlyPlayingId == "alert_sentinel",
                        onClick = {
                            items.find { it.id == "alert_sentinel" }?.let(onPlaySound)
                        }
                    )
                }
            }
        }

        // Emotion Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(emotions) { filter ->
                val isSelected = selectedEmotion == filter.name
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedEmotion = filter.name },
                    label = {
                        Text("${filter.emoji} ${filter.name}")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = filter.color?.copy(alpha = 0.2f)
                            ?: MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = filter.color ?: MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("soundboard_emotion_chip_${filter.name.lowercase()}")
                )
            }
        }

        // Soundboard Grid of Emotion-Mapped Synthesized Barks
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredItems, key = { it.id }) { item ->
                EmotionBarkCard(
                    item = item,
                    isPlaying = currentlyPlayingId == item.id,
                    onTogglePlay = {
                        if (currentlyPlayingId == item.id) {
                            onStopSound()
                        } else {
                            onPlaySound(item)
                        }
                    }
                )
            }
        }
    }
}

private data class EmotionFilter(
    val name: String,
    val emoji: String,
    val color: Color?
)

@Composable
private fun QuickTriggerPill(
    emoji: String,
    label: String,
    color: Color,
    testTag: String,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isPlaying) color else color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) Color.White else color
            )
            if (isPlaying) {
                Icon(
                    Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun EmotionBarkCard(
    item: SoundboardItem,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emotionColor = when (item.emotion.lowercase()) {
        "happy" -> Color(0xFFE65100)
        "playful" -> Color(0xFF00897B)
        "warning" -> Color(0xFFD32F2F)
        "alert" -> Color(0xFFF57C00)
        "curious" -> Color(0xFF5E35B1)
        "calm" -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.primary
    }

    val cardBg by animateColorAsState(
        targetValue = if (isPlaying) emotionColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        label = "card_bg"
    )

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val scale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.05f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playing_pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTogglePlay() }
            .testTag("soundboard_bark_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            width = if (isPlaying) 2.dp else 1.dp,
            color = if (isPlaying) emotionColor else MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Emoji & Play/Stop Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.iconEmoji,
                    fontSize = 32.sp,
                    modifier = Modifier.scale(if (isPlaying) scale else 1.0f)
                )

                Surface(
                    shape = CircleShape,
                    color = if (isPlaying) emotionColor else emotionColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onTogglePlay() }
                        .testTag("play_soundboard_button_${item.id}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop ${item.title}" else "Play ${item.title}",
                            tint = if (isPlaying) Color.White else emotionColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Emotion Tag Pill
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = emotionColor.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(emotionColor, CircleShape)
                    )
                    Text(
                        text = item.emotion.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = emotionColor
                    )
                }
            }

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Canine Meaning
            Text(
                text = "🐾 ${item.canineMeaning}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Context / Meaning description
            Text(
                text = item.humanContext,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Acoustic Metadata & Waveform
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "🔊 ${item.dominantPitchHz}Hz",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                if (isPlaying) {
                    SynthesizerWaveform(color = emotionColor)
                }
            }
        }
    }
}

@Composable
private fun SynthesizerWaveform(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.size(width = 46.dp, height = 18.dp)) {
        val barCount = 7
        val barWidth = size.width / (barCount * 1.5f)
        for (i in 0 until barCount) {
            val hRatio = (0.25f + 0.75f * ((sin(phase + i * 0.9f) + 1f) / 2f)).coerceIn(0.15f, 1f)
            val barHeight = size.height * hRatio
            val x = i * (barWidth * 1.5f)
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}
