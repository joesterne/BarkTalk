package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BarkEasterEgg
import com.example.util.EasterEggCelebrationEvent
import kotlin.math.cos
import kotlin.math.sin

private data class PawConfettiParticle(
    val xFraction: Float,
    val startYFraction: Float,
    val speed: Float,
    val swayAmplitude: Float,
    val phase: Float,
    val radiusDp: Float,
    val colorIndex: Int
)

/**
 * Full-screen non-blocking animated confetti particle burst rendered with DrawScope dimensions
 * when a user triggers an Easter egg celebration.
 */
@Composable
fun PawConfettiCanvas(
    modifier: Modifier = Modifier
) {
    val particles = remember {
        List(28) { idx ->
            PawConfettiParticle(
                xFraction = ((idx * 37 + 13) % 100) / 100f,
                startYFraction = ((idx * 23) % 100) / 100f,
                speed = 0.65f + (idx % 5) * 0.18f,
                swayAmplitude = 14f + (idx % 4) * 8f,
                phase = idx * 0.6f,
                radiusDp = 5f + (idx % 4) * 2.5f,
                colorIndex = idx % 5
            )
        }
    }

    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        Color(0xFFFFB300),
        Color(0xFF26A69A)
    )

    val transition = rememberInfiniteTransition(label = "easter_egg_confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("easter_egg_confetti_canvas")
    ) {
        particles.forEach { p ->
            val yFrac = (p.startYFraction + progress * p.speed) % 1f
            val baseX = p.xFraction * size.width
            val sway = sin((progress * 6.283f) + p.phase) * p.swayAmplitude.dp.toPx()
            val x = (baseX + sway).coerceIn(8f, (size.width - 8f).coerceAtLeast(8f))
            val y = yFrac * size.height
            val color = palette[p.colorIndex].copy(alpha = 0.85f)
            val r = p.radiusDp.dp.toPx()

            // Draw cute paw print cluster (main pad + 3 tiny toe beans)
            drawCircle(color = color, radius = r, center = Offset(x, y))
            val toeR = r * 0.45f
            drawCircle(color = color, radius = toeR, center = Offset(x - r * 0.85f, y - r * 0.95f))
            drawCircle(color = color, radius = toeR, center = Offset(x, y - r * 1.25f))
            drawCircle(color = color, radius = toeR, center = Offset(x + r * 0.85f, y - r * 0.95f))

            // Add subtle floating sparkle ring on alternate particles
            if (p.colorIndex % 2 == 0) {
                val angle = progress * 6.283f + p.phase
                val sx = x + cos(angle) * r * 1.6f
                val sy = y + sin(angle) * r * 1.6f
                drawCircle(color = Color(0xFFFFD54F).copy(alpha = 0.7f), radius = r * 0.3f, center = Offset(sx, sy))
            }
        }
    }
}

/**
 * Celebratory modal dialog displayed when an Easter egg is discovered or triggered.
 */
@Composable
fun EasterEggUnlockedDialog(
    event: EasterEggCelebrationEvent,
    unlockedCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit,
    onOpenVault: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PawConfettiCanvas()

        AlertDialog(
            onDismissRequest = onDismiss,
            modifier = Modifier.testTag("easter_egg_unlocked_dialog"),
            icon = {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(event.egg.emoji, fontSize = 30.sp)
                    }
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = if (event.isFirstDiscovery) "✨ SECRET EASTER EGG UNLOCKED!" else "✨ SECRET EASTER EGG TRIGGERED!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = event.egg.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = event.customMessage ?: event.egg.secretMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.testTag("easter_egg_progress_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Badge: ${event.egg.badgeLabel} • $unlockedCount / $totalCount Found",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("easter_egg_dialog_awesome_button")
                ) {
                    Text("Woof! Awesome!")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        onOpenVault()
                    },
                    modifier = Modifier.testTag("easter_egg_open_vault_button")
                ) {
                    Text("Secret Vault ($unlockedCount/$totalCount)")
                }
            }
        )
    }
}

/**
 * Secret "Good Boy Vault" dialog showcasing discovered Easter eggs, cryptic hints
 * for locked secrets, and a Zoomies Mode toggle once unlocked.
 */
@Composable
fun EasterEggVaultDialog(
    unlockedEggs: Set<BarkEasterEgg>,
    zoomiesModeEnabled: Boolean,
    onToggleZoomiesMode: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("easter_egg_vault_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Secret Good Boy Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${unlockedEggs.size} of ${BarkEasterEgg.entries.size} hidden secrets discovered",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (BarkEasterEgg.SNOOT_BOOP_ZOOMIES in unlockedEggs) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "⚡ Turbo Zoomies Badge",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Show celebratory Zoomies flair in the top bar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = zoomiesModeEnabled,
                                onCheckedChange = { onToggleZoomiesMode() },
                                modifier = Modifier.testTag("zoomies_mode_switch")
                            )
                        }
                    }
                }

                BarkEasterEgg.entries.forEach { egg ->
                    val isUnlocked = egg in unlockedEggs
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isUnlocked) {
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isUnlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_egg_${egg.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isUnlocked) {
                                        Text(egg.emoji, fontSize = 18.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked Easter Egg",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isUnlocked) egg.title else "Locked Canine Secret",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isUnlocked) egg.secretMessage else "Hint: ${egg.crypticHint}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_easter_egg_vault_button")
            ) {
                Text("Close Vault")
            }
        }
    )
}
