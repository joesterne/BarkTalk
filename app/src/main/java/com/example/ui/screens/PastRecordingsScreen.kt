package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.PictureAsPdf
import com.example.data.RecordedBark
import com.example.data.TranslationLog
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.ShareTranslationDialog
import com.example.util.BarkHistoryPdfExporter.toTranslationLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen that displays a list of past dog bark recordings stored in the Room database,
 * prominently showing each recording's timestamp and AI-generated translation.
 */
@Composable
fun PastRecordingsScreen(
    viewModel: BarkTalkViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRecord: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (onNavigateBack != null) {
        BackHandler { onNavigateBack() }
    }

    val recordedBarks by viewModel.recordedBarks.collectAsState()
    val currentlyPlayingBarkPath by viewModel.currentlyPlayingBarkPath.collectAsState()

    PastRecordingsListContent(
        recordings = recordedBarks,
        currentlyPlayingBarkPath = currentlyPlayingBarkPath,
        onPlayAudio = { path -> viewModel.playRecordedBark(path) },
        onStopAudio = { viewModel.stopPlayingRecordedBark() },
        onSpeakTranslation = { text -> viewModel.speakTranslation(text) },
        onToggleFavorite = { bark -> viewModel.toggleRecordedBarkFavorite(bark) },
        onDeleteRecording = { bark -> viewModel.deleteRecordedBark(bark) },
        onClearAllRecordings = { viewModel.clearAllLogs() },
        onNavigateBack = onNavigateBack,
        onNavigateToRecord = onNavigateToRecord,
        modifier = modifier
    )
}

/**
 * Stateless, testable Compose layout displaying the list of past recordings from Room
 * with their timestamp and AI-generated translation.
 */
@Composable
fun PastRecordingsListContent(
    recordings: List<RecordedBark>,
    currentlyPlayingBarkPath: String? = null,
    onPlayAudio: (String) -> Unit = {},
    onStopAudio: () -> Unit = {},
    onSpeakTranslation: (String) -> Unit = {},
    onToggleFavorite: (RecordedBark) -> Unit = {},
    onDeleteRecording: (RecordedBark) -> Unit = {},
    onClearAllRecordings: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToRecord: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var shareTargetBark by remember { mutableStateOf<RecordedBark?>(null) }

    val filteredRecordings = remember(recordings, searchQuery, selectedFilter) {
        recordings
            .filter { bark ->
                when (selectedFilter) {
                    "Favorites" -> bark.isFavorite
                    "Urgent" -> bark.urgencyLevel.equals("HIGH", ignoreCase = true) ||
                        bark.urgencyLevel.equals("CRITICAL", ignoreCase = true)
                    else -> true
                }
            }
            .filter { bark ->
                if (searchQuery.isBlank()) true
                else {
                    bark.translation.contains(searchQuery, ignoreCase = true) ||
                        bark.petName.contains(searchQuery, ignoreCase = true) ||
                        bark.vocalizationType.contains(searchQuery, ignoreCase = true) ||
                        bark.detectedEmotion.contains(searchQuery, ignoreCase = true)
                }
            }
    }

    val timestampFormatter = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("past_recordings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Screen Header Row
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
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("past_recordings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }

                Column {
                    Text(
                        text = "Past Bark Recordings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredRecordings.size} stored in Room database with timestamp & translation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showPdfExportDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("export_past_recordings_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Export recordings to PDF",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (recordings.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearConfirmDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("clear_past_recordings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all past recordings",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Summary Telemetry Bar
        if (recordings.isNotEmpty()) {
            val avgDb = recordings.map { it.peakDb }.average().toInt()
            val favCount = recordings.count { it.isFavorite }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RecordingStatItem(label = "Total Stored", value = "${recordings.size} Barks")
                    RecordingStatItem(label = "Avg Intensity", value = "$avgDb dB")
                    RecordingStatItem(label = "Bookmarked", value = "$favCount ⭐")
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search translations, dog name, or bark type...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("past_recordings_search_input")
        )

        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == "All",
                    onClick = { selectedFilter = "All" },
                    label = { Text("All Recordings (${recordings.size})") },
                    modifier = Modifier.testTag("recordings_filter_all")
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "Favorites",
                    onClick = { selectedFilter = "Favorites" },
                    label = { Text("⭐ Favorites (${recordings.count { it.isFavorite }})") },
                    modifier = Modifier.testTag("recordings_filter_favorites")
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "Urgent",
                    onClick = { selectedFilter = "Urgent" },
                    label = { Text("🚨 High Urgency") },
                    modifier = Modifier.testTag("recordings_filter_urgent")
                )
            }
        }

        // Recordings List or Empty State
        if (filteredRecordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("past_recordings_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No matching bark recordings found"
                        } else {
                            "No past recordings stored yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Record your dog's barks using the microphone studio to automatically save timestamps and AI-generated translations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (onNavigateToRecord != null) {
                        Button(
                            onClick = onNavigateToRecord,
                            modifier = Modifier.testTag("empty_state_record_bark_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Record a Dog Bark")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("past_recordings_list")
            ) {
                items(filteredRecordings, key = { it.id }) { bark ->
                    val formattedTimestamp = remember(bark.timestamp) {
                        timestampFormatter.format(Date(bark.timestamp))
                    }
                    val hasPlayableAudio = remember(bark.audioFilePath) {
                        !bark.audioFilePath.isNullOrBlank() && File(bark.audioFilePath).exists()
                    }
                    val isPlaying = hasPlayableAudio && currentlyPlayingBarkPath == bark.audioFilePath

                    PastRecordingItemCard(
                        bark = bark,
                        formattedTimestamp = formattedTimestamp,
                        hasPlayableAudio = hasPlayableAudio,
                        isPlayingAudio = isPlaying,
                        onPlayAudio = { bark.audioFilePath?.let(onPlayAudio) },
                        onStopAudio = onStopAudio,
                        onSpeakTranslation = { onSpeakTranslation(bark.translation) },
                        onShare = { shareTargetBark = bark },
                        onToggleFavorite = { onToggleFavorite(bark) },
                        onDelete = { onDeleteRecording(bark) }
                    )
                }
            }
        }
    }

    if (showPdfExportDialog) {
        PdfExportDialog(
            logs = filteredRecordings.map { it.toTranslationLog() },
            onDismissRequest = { showPdfExportDialog = false }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Past Recordings?") },
            text = {
                Text("This will permanently remove all stored dog bark recordings and their translations from the Room database.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllRecordings()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_recordings_button")
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    shareTargetBark?.let { bark ->
        val shareLog = TranslationLog(
            id = bark.id,
            petId = bark.petId,
            petName = bark.petName,
            petBreed = bark.petBreed,
            timestamp = bark.timestamp,
            vocalizationType = bark.vocalizationType,
            dominantPitchHz = bark.dominantPitchHz,
            intensityDb = bark.peakDb,
            cadenceDescription = bark.cadenceDescription,
            englishTranslation = bark.translation,
            dogThought = bark.dogThought,
            detectedEmotion = bark.detectedEmotion,
            urgencyLevel = bark.urgencyLevel,
            confidencePercent = bark.confidencePercent,
            isFavorite = bark.isFavorite,
            audioFilePath = bark.audioFilePath
        )
        ShareTranslationDialog(
            log = shareLog,
            onDismissRequest = { shareTargetBark = null }
        )
    }
}

@Composable
private fun RecordingStatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PastRecordingItemCard(
    bark: RecordedBark,
    formattedTimestamp: String,
    hasPlayableAudio: Boolean,
    isPlayingAudio: Boolean,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit,
    onSpeakTranslation: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgencyColor = when (bark.urgencyLevel.uppercase()) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "HIGH" -> MaterialTheme.colorScheme.tertiary
        "MEDIUM" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("past_recording_card_${bark.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Dog Profile + Recording Timestamp Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${bark.petName} (${bark.petBreed})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = formattedTimestamp,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("recording_timestamp_${bark.id}")
                        )
                    }
                }
            }

            // AI-Generated Translation Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🗣️ AI-Generated Translation",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = urgencyColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = bark.urgencyLevel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = urgencyColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "\"${bark.translation}\"",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("recording_translation_${bark.id}")
                    )

                    if (bark.dogThought.isNotBlank()) {
                        Text(
                            text = "💭 ${bark.dogThought}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Acoustic Telemetry Row & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${bark.vocalizationType} • ${bark.peakDb}dB • ${bark.dominantPitchHz}Hz",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasPlayableAudio) {
                        IconButton(
                            onClick = { if (isPlayingAudio) onStopAudio() else onPlayAudio() },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("play_past_recording_${bark.id}")
                        ) {
                            Icon(
                                imageVector = if (isPlayingAudio) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingAudio) "Stop recording" else "Play recording",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    IconButton(
                        onClick = onSpeakTranslation,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("speak_past_recording_${bark.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak translation aloud",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("share_past_recording_${bark.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share translation",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("favorite_past_recording_${bark.id}")
                    ) {
                        Icon(
                            imageVector = if (bark.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (bark.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_past_recording_${bark.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete recording",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
