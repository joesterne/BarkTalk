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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundboardItem
import kotlin.math.sin

/**
 * Grid-based Soundboard UI embedded in the main Compose layout that allows users
 * to tap and play pre-recorded dog sounds immediately via the device speaker.
 */
@Composable
fun DogSoundboardGridCard(
    items: List<SoundboardItem>,
    currentlyPlayingId: String?,
    lastPlayedSound: SoundboardItem?,
    onPlaySound: (SoundboardItem) -> Unit,
    onStopSound: () -> Unit,
    onOpenFullSoundboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var gridColumns by remember { mutableIntStateOf(3) }

    val categories = remember {
        listOf(
            "All" to "🐾",
            "Happy" to "🐶",
            "Playful" to "🎾",
            "Warning" to "⚠️",
            "Alert" to "🚨",
            "Curious" to "🧐",
            "Calm" to "💤"
        )
    }

    val filteredItems = remember(selectedCategory, items) {
        if (selectedCategory == "All") items
        else items.filter { it.emotion.equals(selectedCategory, ignoreCase = true) }
    }

    val activeSound = remember(currentlyPlayingId, lastPlayedSound, items) {
        items.find { it.id == currentlyPlayingId } ?: lastPlayedSound
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("main_soundboard_grid_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Title, Column Toggle & Full Studio Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Canine Soundboard Grid",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap any pad to play pre-recorded dog sounds",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { gridColumns = if (gridColumns == 3) 2 else 3 },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("toggle_soundboard_grid_columns_button")
                    ) {
                        Icon(
                            imageVector = if (gridColumns == 3) Icons.Default.GridView else Icons.Default.ViewModule,
                            contentDescription = "Switch grid layout density",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenFullSoundboard,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_full_soundboard_button")
                    ) {
                        Text("Expand", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Active / Last Played Pre-Recorded Sound Status Banner
            AnimatedVisibility(
                visible = activeSound != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeSound?.let { sound ->
                    val isCurrentlyPlaying = currentlyPlayingId == sound.id
                    val accentColor = soundboardEmotionColor(sound.emotion)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isCurrentlyPlaying) {
                            accentColor.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isCurrentlyPlaying) accentColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("soundboard_active_status_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(sound.iconEmoji, fontSize = 24.sp)
                                Column {
                                    Text(
                                        text = if (isCurrentlyPlaying) {
                                            "Playing: ${sound.title}"
                                        } else {
                                            "Last Played: ${sound.title}"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "🐾 ${sound.canineMeaning} • ${sound.dominantPitchHz}Hz",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (isCurrentlyPlaying) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MiniPadWaveform(color = accentColor)
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clickable { onStopSound() }
                                            .testTag("stop_active_soundboard_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Stop,
                                                contentDescription = "Stop Sound",
                                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = accentColor.copy(alpha = 0.16f),
                                    modifier = Modifier
                                        .clickable { onPlaySound(sound) }
                                        .testTag("replay_last_soundboard_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Replay ${sound.title}",
                                            tint = accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Replay",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Emotion Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("main_soundboard_filter_row")
            ) {
                items(categories) { (category, emoji) ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text("$emoji $category") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("main_soundboard_filter_${category.lowercase()}")
                    )
                }
            }

            // Interactive LazyVerticalGrid of Pre-Recorded Dog Sound Pads
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .testTag("main_soundboard_grid")
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    SoundboardGridPad(
                        item = item,
                        isPlaying = currentlyPlayingId == item.id,
                        compactMode = gridColumns >= 3,
                        onTap = {
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
}

@Composable
fun SoundboardGridPad(
    item: SoundboardItem,
    isPlaying: Boolean,
    compactMode: Boolean = false,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emotionColor = soundboardEmotionColor(item.emotion)
    val containerColor by animateColorAsState(
        targetValue = if (isPlaying) {
            emotionColor.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        label = "pad_bg"
    )

    val pulseTransition = rememberInfiniteTransition(label = "pad_pulse")
    val emojiScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isPlaying) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isPlaying) 2.dp else 1.dp,
            color = if (isPlaying) emotionColor else MaterialTheme.colorScheme.outlineVariant
        ),
        shadowElevation = if (isPlaying) 4.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onTap() }
            .testTag("soundboard_grid_item_${item.id}")
    ) {
        if (compactMode) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.TopEnd,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.iconEmoji,
                            fontSize = 28.sp,
                            modifier = Modifier.scale(if (isPlaying) emojiScale else 1.0f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) emotionColor else emotionColor.copy(alpha = 0.16f),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop ${item.title}" else "Play ${item.title}",
                                tint = if (isPlaying) Color.White else emotionColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = emotionColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isPlaying) "PLAYING" else item.emotion.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = emotionColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.iconEmoji,
                        fontSize = 28.sp,
                        modifier = Modifier.scale(if (isPlaying) emojiScale else 1.0f)
                    )

                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) emotionColor else emotionColor.copy(alpha = 0.16f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Stop ${item.title}" else "Play ${item.title}",
                                tint = if (isPlaying) Color.White else emotionColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.canineMeaning,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = emotionColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.emotion.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = emotionColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${item.dominantPitchHz}Hz",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniPadWaveform(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.size(width = 36.dp, height = 16.dp)) {
        val barCount = 6
        val barWidth = size.width / (barCount * 1.5f)
        for (i in 0 until barCount) {
            val hRatio = (0.25f + 0.75f * ((sin(phase + i * 0.9f) + 1f) / 2f)).coerceIn(0.2f, 1f)
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

fun soundboardEmotionColor(emotion: String): Color {
    return when (emotion.lowercase()) {
        "happy" -> Color(0xFFE65100)
        "playful" -> Color(0xFF00897B)
        "warning" -> Color(0xFFD32F2F)
        "alert" -> Color(0xFFF57C00)
        "curious" -> Color(0xFF5E35B1)
        "calm" -> Color(0xFF2E7D32)
        else -> Color(0xFF1E88E5)
    }
}
