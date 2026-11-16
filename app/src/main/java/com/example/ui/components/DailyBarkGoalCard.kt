package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationLog
import com.example.util.SocialShareHelper
import kotlin.math.roundToInt

/**
 * Displays the user's daily bark analysis goal, interactive controls to set/adjust the target
 * number of barks to analyze per day, and an animated progress bar indicating how close they
 * are to reaching their daily goal.
 */
@Composable
fun DailyBarkGoalCard(
    todayAnalyzedCount: Int,
    dailyGoal: Int,
    onUpdateDailyGoal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val safeGoal = dailyGoal.coerceIn(1, 50)
    val safeAnalyzed = todayAnalyzedCount.coerceAtLeast(0)
    val isGoalReached = safeAnalyzed >= safeGoal
    val remaining = (safeGoal - safeAnalyzed).coerceAtLeast(0)
    val targetFraction = (safeAnalyzed.toFloat() / safeGoal.toFloat()).coerceIn(0f, 1f)
    val percentComplete = (targetFraction * 100f).roundToInt()

    val animatedProgress by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "daily_goal_progress"
    )

    var showCustomGoalDialog by remember { mutableStateOf(false) }

    val progressColor = if (isGoalReached) {
        Color(0xFF2E7D32)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_bark_goal_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isGoalReached) {
                Color(0xFF2E7D32).copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Row: Goal Title + Status Badge + Edit Goal Button
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
                        color = if (isGoalReached) {
                            Color(0xFF2E7D32).copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isGoalReached) Icons.Default.CheckCircle else Icons.Default.Flag,
                                contentDescription = null,
                                tint = if (isGoalReached) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Daily Bark Analysis Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isGoalReached) {
                                "🎉 Daily goal reached! Great job tracking your dog's speech!"
                            } else {
                                "$remaining more ${if (remaining == 1) "bark" else "barks"} to reach today's goal"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isGoalReached) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isGoalReached) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.testTag("daily_goal_status_badge")
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showCustomGoalDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("set_daily_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Set Daily Bark Goal",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Set Goal", style = MaterialTheme.typography.labelMedium)
                }
            }

            // 2. Progress Bar Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$safeAnalyzed / $safeGoal barks analyzed today",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("daily_bark_goal_progress_text")
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = progressColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "$percentComplete%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = progressColor,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("daily_bark_goal_percent_badge")
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .semantics {
                            contentDescription =
                                "Daily bark analysis progress: $safeAnalyzed of $safeGoal barks analyzed ($percentComplete percent)"
                        }
                        .testTag("daily_bark_goal_progress_bar"),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // 3. Inline Stepper + Quick Goal Presets Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(3, 5, 10, 15).forEach { preset ->
                        FilterChip(
                            selected = safeGoal == preset,
                            onClick = { onUpdateDailyGoal(preset) },
                            label = {
                                Text(
                                    text = "$preset/day",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (safeGoal == preset) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            modifier = Modifier.testTag("daily_goal_preset_$preset")
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onUpdateDailyGoal((safeGoal - 1).coerceAtLeast(1)) },
                        enabled = safeGoal > 1,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("decrease_daily_goal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease daily bark goal"
                        )
                    }

                    IconButton(
                        onClick = { onUpdateDailyGoal((safeGoal + 1).coerceAtMost(50)) },
                        enabled = safeGoal < 50,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("increase_daily_goal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase daily bark goal"
                        )
                    }
                }
            }
        }
    }

    if (showCustomGoalDialog) {
        var goalInput by remember(safeGoal) { mutableStateOf(safeGoal.toString()) }
        AlertDialog(
            onDismissRequest = { showCustomGoalDialog = false },
            modifier = Modifier.testTag("daily_goal_dialog"),
            title = {
                Text(
                    text = "Set Daily Bark Analysis Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the target number of dog barks you want to record and analyze each day (1–50):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = goalInput,
                        onValueChange = { value ->
                            goalInput = value.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Daily Barks Target") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_goal_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = goalInput.toIntOrNull()?.coerceIn(1, 50) ?: safeGoal
                        onUpdateDailyGoal(parsed)
                        showCustomGoalDialog = false
                    },
                    modifier = Modifier.testTag("save_daily_goal_button")
                ) {
                    Text("Save Goal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Dedicated Quick-Share Card allowing users to immediately share their latest bark translation
 * via social media or messaging apps using Android's ACTION_SEND ShareSheet Intent.
 */
@Composable
fun LatestBarkQuickShareCard(
    latestLog: TranslationLog?,
    onOpenFormatDialog: (TranslationLog) -> Unit,
    onToggleFavorite: ((TranslationLog) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (latestLog == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("latest_bark_quick_share_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📢", fontSize = 18.sp)
                    Column {
                        Text(
                            text = "Latest Bark Translation (${latestLog.petName})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${latestLog.vocalizationType} • ${latestLog.intensityDb} dB • ${latestLog.detectedEmotion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                if (onToggleFavorite != null) {
                    IconButton(
                        onClick = { onToggleFavorite(latestLog) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("favorite_quick_share_card_button")
                    ) {
                        Icon(
                            imageVector = if (latestLog.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (latestLog.isFavorite) "Remove from favorites" else "Mark as favorite",
                            tint = if (latestLog.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${latestLog.englishTranslation}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(12.dp)
                        .testTag("latest_bark_translation_preview_text")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        SocialShareHelper.quickShareLatestTranslation(context, latestLog)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("quick_share_latest_bark_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Quick Share Latest Bark")
                }

                OutlinedButton(
                    onClick = { onOpenFormatDialog(latestLog) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("customize_latest_share_button")
                ) {
                    Text("Formats")
                }
            }
        }
    }
}
