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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TranslationLog
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.DogTagBadge
import com.example.ui.components.DogTagEditorDialog
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.ShareTranslationDialog
import com.example.ui.components.WeeklyBarkFrequencyCalculator
import com.example.ui.components.WeeklyBarkFrequencyChart
import com.example.util.BarkHistoryPdfExporter.toTranslationLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: BarkTalkViewModel,
    onNavigateToRecord: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.translationLogs.collectAsStateWithLifecycle()
    val recordedBarks by viewModel.recordedBarks.collectAsStateWithLifecycle()
    val assignedDogTags by viewModel.assignedDogTags.collectAsStateWithLifecycle()
    val petProfiles by viewModel.petProfiles.collectAsStateWithLifecycle()
    val currentlyPlayingBarkPath by viewModel.currentlyPlayingBarkPath.collectAsStateWithLifecycle()

    var historyTabMode by remember { mutableStateOf("Recordings") } // "Recordings" or "Logs"
    var showPdfExportDialog by remember { mutableStateOf(false) }

    val suggestedTags = remember(assignedDogTags, petProfiles) {
        (assignedDogTags + petProfiles.map { it.name })
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }

    val weeklyBuckets = remember(allLogs, recordedBarks) {
        WeeklyBarkFrequencyCalculator.computeLast7DaysCombined(
            logs = allLogs,
            recordings = recordedBarks
        )
    }

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
            TranslationLogsHistoryContent(
                logs = allLogs,
                weeklyBuckets = weeklyBuckets,
                suggestedTags = suggestedTags,
                currentlyPlayingBarkPath = currentlyPlayingBarkPath,
                onPlayBark = { path -> viewModel.playRecordedBark(path) },
                onStopBark = { viewModel.stopPlayingRecordedBark() },
                onSpeak = { text -> viewModel.speakTranslation(text) },
                onToggleFavorite = { log -> viewModel.toggleFavorite(log) },
                onUpdateDogTag = { log, newTag -> viewModel.updateLogDogTag(log, newTag) },
                onDelete = { log -> viewModel.deleteLog(log) },
                onClearAllLogs = { viewModel.clearAllLogs() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showPdfExportDialog) {
        val logsToExport = if (historyTabMode == "Recordings" && recordedBarks.isNotEmpty()) {
            recordedBarks.map { it.toTranslationLog() }
        } else {
            allLogs
        }
        PdfExportDialog(
            logs = logsToExport,
            onDismissRequest = { showPdfExportDialog = false }
        )
    }
}

/**
 * Stateless, testable Compose layout displaying translated bark history logs stored in Room,
 * including translated text, timestamp, and optional user-assigned dog tags with live filtering.
 */
@Composable
fun TranslationLogsHistoryContent(
    logs: List<TranslationLog>,
    weeklyBuckets: List<com.example.ui.components.DailyBarkFrequencyBucket> = emptyList(),
    suggestedTags: List<String> = emptyList(),
    currentlyPlayingBarkPath: String? = null,
    onPlayBark: (String) -> Unit = {},
    onStopBark: () -> Unit = {},
    onSpeak: (String) -> Unit = {},
    onToggleFavorite: (TranslationLog) -> Unit = {},
    onUpdateDogTag: (TranslationLog, String?) -> Unit = { _, _ -> },
    onDelete: (TranslationLog) -> Unit = {},
    onClearAllLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("All") } // "All", "Favorites", "Urgent", "Tagged"
    var selectedDogTagFilter by remember { mutableStateOf<String?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var shareDialogLog by remember { mutableStateOf<TranslationLog?>(null) }
    var tagEditorTargetLog by remember { mutableStateOf<TranslationLog?>(null) }

    val distinctTags = remember(logs) {
        logs.mapNotNull { it.dogTag?.trim()?.takeIf { tag -> tag.isNotEmpty() } }
            .distinctBy { it.lowercase() }
            .sorted()
    }

    val filteredLogs = remember(logs, filterMode, selectedDogTagFilter, searchQuery) {
        logs
            .filter { log ->
                when (filterMode) {
                    "Favorites" -> log.isFavorite
                    "Urgent" -> log.urgencyLevel == "HIGH" || log.urgencyLevel == "CRITICAL"
                    "Tagged" -> !log.dogTag.isNullOrBlank()
                    else -> true
                }
            }
            .filter { log ->
                if (selectedDogTagFilter == null) true
                else log.dogTag.equals(selectedDogTagFilter, ignoreCase = true)
            }
            .filter { log ->
                if (searchQuery.isBlank()) true
                else {
                    log.englishTranslation.contains(searchQuery, ignoreCase = true) ||
                        log.dogThought.contains(searchQuery, ignoreCase = true) ||
                        log.petName.contains(searchQuery, ignoreCase = true) ||
                        (log.dogTag?.contains(searchQuery, ignoreCase = true) == true)
                }
            }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("translation_logs_history_content")
    ) {
        // Header with Clear Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Translation History Log",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                val taggedCount = logs.count { !it.dogTag.isNullOrBlank() }
                Text(
                    text = "${filteredLogs.size} logs in Room • $taggedCount tagged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (logs.isNotEmpty()) {
                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Search Bar (matches translated text, thought, pet name, or optional dog tag)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search translation text or #dog tag...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_logs_search_input")
        )

        Spacer(Modifier.height(8.dp))

        // Filters: All, Favorites, Urgent, Tagged, and individual User-Assigned Dog Tags
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("history_filter_chips_row")
        ) {
            item {
                FilterChip(
                    selected = filterMode == "All" && selectedDogTagFilter == null,
                    onClick = {
                        filterMode = "All"
                        selectedDogTagFilter = null
                    },
                    label = { Text("All (${logs.size})") },
                    modifier = Modifier.testTag("filter_all")
                )
            }
            item {
                FilterChip(
                    selected = filterMode == "Favorites" && selectedDogTagFilter == null,
                    onClick = {
                        filterMode = "Favorites"
                        selectedDogTagFilter = null
                    },
                    label = { Text("⭐ Favorites (${logs.count { it.isFavorite }})") },
                    modifier = Modifier.testTag("filter_favorites")
                )
            }
            item {
                FilterChip(
                    selected = filterMode == "Urgent" && selectedDogTagFilter == null,
                    onClick = {
                        filterMode = "Urgent"
                        selectedDogTagFilter = null
                    },
                    label = { Text("🚨 Urgent") },
                    modifier = Modifier.testTag("filter_urgent")
                )
            }
            item {
                FilterChip(
                    selected = filterMode == "Tagged" && selectedDogTagFilter == null,
                    onClick = {
                        filterMode = "Tagged"
                        selectedDogTagFilter = null
                    },
                    label = { Text("🏷️ Tagged (${logs.count { !it.dogTag.isNullOrBlank() }})") },
                    modifier = Modifier.testTag("filter_tagged")
                )
            }
            items(distinctTags, key = { "tag_filter_$it" }) { tag ->
                val isSelected = selectedDogTagFilter.equals(tag, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            selectedDogTagFilter = null
                        } else {
                            filterMode = "All"
                            selectedDogTagFilter = tag
                        }
                    },
                    label = { Text("#$tag") },
                    modifier = Modifier.testTag("filter_dog_tag_${tag.lowercase().replace(" ", "_")}")
                )
            }
        }

        Spacer(Modifier.height(10.dp))

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
                        text = when {
                            selectedDogTagFilter != null -> "No logs tagged #$selectedDogTagFilter"
                            filterMode == "Favorites" -> "No favorite translations yet"
                            filterMode == "Tagged" -> "No tagged bark logs yet"
                            else -> "No bark translations recorded yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Listen with the microphone or tap 'Add Dog Tag' on any log to organize translations!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("translation_logs_list")
            ) {
                if (weeklyBuckets.isNotEmpty()) {
                    item(key = "weekly_bark_chart_header") {
                        WeeklyBarkFrequencyChart(buckets = weeklyBuckets)
                    }
                }

                items(filteredLogs, key = { it.id }) { log ->
                    val isPlayingBark = !log.audioFilePath.isNullOrBlank() && currentlyPlayingBarkPath == log.audioFilePath
                    HistoryCard(
                        log = log,
                        dateString = dateFormatter.format(Date(log.timestamp)),
                        isPlayingBark = isPlayingBark,
                        onPlayBark = onPlayBark,
                        onStopBark = onStopBark,
                        onSpeak = { onSpeak(log.englishTranslation) },
                        onShare = { shareDialogLog = log },
                        onEditDogTag = { tagEditorTargetLog = log },
                        onToggleFavorite = { onToggleFavorite(log) },
                        onDelete = { onDelete(log) }
                    )
                }
            }
        }
    }

    tagEditorTargetLog?.let { targetLog ->
        DogTagEditorDialog(
            initialTag = targetLog.dogTag,
            translatedTextPreview = targetLog.englishTranslation,
            suggestedTags = (suggestedTags + distinctTags).distinctBy { it.lowercase() },
            onSaveTag = { updatedTag ->
                onUpdateDogTag(targetLog, updatedTag)
                tagEditorTargetLog = null
            },
            onDismiss = { tagEditorTargetLog = null }
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
                        onClearAllLogs()
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
    onEditDogTag: () -> Unit = {},
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
            // Header Row: Dog Name & Urgency Pill
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
                    modifier = Modifier
                        .padding(12.dp)
                        .testTag("history_translation_text_${log.id}")
                )
            }

            // Inner dog thought
            if (log.dogThought.isNotBlank()) {
                Text(
                    text = "💭 ${log.dogThought}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Acoustic metadata & Timestamp row
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("history_timestamp_${log.id}")
                )
            }

            // Optional User-Assigned Dog Tag Pill + Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DogTagBadge(
                    dogTag = log.dogTag,
                    onClick = onEditDogTag,
                    testTagName = "history_dog_tag_badge_${log.id}"
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!log.audioFilePath.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                if (isPlayingBark) onStopBark() else onPlayBark(log.audioFilePath)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlayingBark) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingBark) "Stop Bark Audio" else "Play Bark Audio",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(onClick = onSpeak, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = "Speak Aloud",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onShare, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (log.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (log.isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
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
}
