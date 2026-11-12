package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.FilledTonalButton
import com.example.data.TranslationLog
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.ShareTranslationDialog
import com.example.util.BarkHistoryPdfExporter.toTranslationLog
import com.example.util.SocialShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: BarkTalkViewModel,
    onNavigateToRecord: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allLogs by viewModel.translationLogs.collectAsState()
    val recordedBarks by viewModel.recordedBarks.collectAsState()
    var historyTabMode by remember { mutableStateOf("Recordings") } // "Recordings" or "Logs"
    var filterMode by remember { mutableStateOf("All") } // "All", "Favorites", "Urgent"
    var showClearDialog by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var shareDialogLog by remember { mutableStateOf<TranslationLog?>(null) }

    val filteredLogs = remember(allLogs, filterMode) {
        when (filterMode) {
            "Favorites" -> allLogs.filter { it.isFavorite }
            "Urgent" -> allLogs.filter { it.urgencyLevel == "HIGH" || it.urgencyLevel == "CRITICAL" }
            else -> allLogs
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    val currentlyPlayingBarkPath by viewModel.currentlyPlayingBarkPath.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("history_screen")
    ) {
        // Segmented Switcher + Export PDF Action Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                FilterChip(
                    selected = historyTabMode == "Recordings",
                    onClick = { historyTabMode = "Recordings" },
                    label = { Text("🎙️ Recordings (${recordedBarks.size})") },
                    modifier = Modifier.testTag("tab_past_recordings")
                )
                FilterChip(
                    selected = historyTabMode == "Logs",
                    onClick = { historyTabMode = "Logs" },
                    label = { Text("📜 Logs (${allLogs.size})") },
                    modifier = Modifier.testTag("tab_translation_logs")
                )
            }

            FilledTonalButton(
                onClick = { showPdfExportDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("export_history_pdf_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PictureAsPdf,
                    contentDescription = "Export History to PDF",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("PDF", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (historyTabMode == "Recordings") {
            PastRecordingsScreen(
                viewModel = viewModel,
                onNavigateToRecord = onNavigateToRecord,
                modifier = Modifier.fillMaxSize()
            )
        } else {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header with Clear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Translation History Log",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredLogs.size} translations recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (allLogs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear History",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

        // Filters: All, Favorites, High Urgency
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = filterMode == "All",
                onClick = { filterMode = "All" },
                label = { Text("All (${allLogs.size})") },
                modifier = Modifier.testTag("filter_all")
            )
            FilterChip(
                selected = filterMode == "Favorites",
                onClick = { filterMode = "Favorites" },
                label = { Text("⭐ Favorites (${allLogs.count { it.isFavorite }})") },
                modifier = Modifier.testTag("filter_favorites")
            )
            FilterChip(
                selected = filterMode == "Urgent",
                onClick = { filterMode = "Urgent" },
                label = { Text("🚨 Urgent Alerts") },
                modifier = Modifier.testTag("filter_urgent")
            )
        }

        Spacer(Modifier.height(12.dp))

        // History Log List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("empty_history_view"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🐾", fontSize = 48.sp)
                    Text(
                        text = if (filterMode == "Favorites") "No favorite translations yet" else "No bark translations recorded yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Listen with the microphone or try the Bark Simulator!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    val isPlayingBark = !log.audioFilePath.isNullOrBlank() && currentlyPlayingBarkPath == log.audioFilePath
                    HistoryCard(
                        log = log,
                        dateString = dateFormatter.format(Date(log.timestamp)),
                        isPlayingBark = isPlayingBark,
                        onPlayBark = { path -> viewModel.playRecordedBark(path) },
                        onStopBark = { viewModel.stopPlayingRecordedBark() },
                        onSpeak = { viewModel.speakTranslation(log.englishTranslation) },
                        onShare = { shareDialogLog = log },
                        onToggleFavorite = { viewModel.toggleFavorite(log) },
                        onDelete = { viewModel.deleteLog(log) }
                    )
                }
            }
        }
        }
        }
    }

    if (showPdfExportDialog) {
        val logsToExport = if (historyTabMode == "Recordings" && recordedBarks.isNotEmpty()) {
            recordedBarks.map { it.toTranslationLog() }
        } else {
            filteredLogs
        }
        PdfExportDialog(
            logs = logsToExport,
            onDismissRequest = { showPdfExportDialog = false }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Translation Logs?") },
            text = { Text("This will permanently remove all saved bark and whine translation logs.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllLogs()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    shareDialogLog?.let { logToShare ->
        ShareTranslationDialog(
            log = logToShare,
            onDismissRequest = { shareDialogLog = null }
        )
    }
}

@Composable
fun HistoryCard(
    log: TranslationLog,
    dateString: String,
    isPlayingBark: Boolean = false,
    onPlayBark: (String) -> Unit = {},
    onStopBark: () -> Unit = {},
    onSpeak: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    val urgencyColor = when (log.urgencyLevel) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "HIGH" -> MaterialTheme.colorScheme.tertiary
        "MEDIUM" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_item_${log.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Dog Name & Timestamp + Urgency Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${log.petName} (${log.petBreed})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = urgencyColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = log.urgencyLevel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // English translation quotation
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${log.englishTranslation}\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Inner dog thought
            Text(
                text = "💭 ${log.dogThought}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Acoustic metadata pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "🎵 ${log.dominantPitchHz}Hz • ${log.vocalizationType}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "• $dateString",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Bar: Play Bark, Speak, Share, Favorite, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!log.audioFilePath.isNullOrBlank()) {
                    IconButton(
                        onClick = {
                            if (isPlayingBark) onStopBark() else onPlayBark(log.audioFilePath)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingBark) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingBark) "Stop Bark Audio" else "Play Bark Audio",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(onClick = onSpeak, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Speak Aloud",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (log.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (log.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Log",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
