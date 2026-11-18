package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationLog
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.ShareTranslationDialog
import com.example.util.BarkHistoryPdfExporter.toTranslationLog
import com.example.util.SocialShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated 'Favorites' tab screen where users can view, search, play, share, and mark
 * specific bark translations as favorites for quick access.
 */
@Composable
fun FavoritesScreen(
    viewModel: BarkTalkViewModel,
    onNavigateToTranslate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val favoriteTranslations by viewModel.favoriteTranslations.collectAsStateWithLifecycle()
    val favoriteRecordedBarks by viewModel.favoriteRecordedBarks.collectAsStateWithLifecycle()
    val allLogs by viewModel.translationLogs.collectAsStateWithLifecycle()
    val currentlyPlayingBarkPath by viewModel.currentlyPlayingBarkPath.collectAsStateWithLifecycle()

    // Merge favorite logs and any favorite recorded barks not already in favoriteTranslations
    val mergedFavorites = remember(favoriteTranslations, favoriteRecordedBarks) {
        val list = favoriteTranslations.toMutableList()
        favoriteRecordedBarks.forEach { bark ->
            val alreadyPresent = list.any { existing ->
                kotlin.math.abs(existing.timestamp - bark.timestamp) <= 2000L &&
                    existing.englishTranslation == bark.translation
            }
            if (!alreadyPresent) {
                list.add(bark.toTranslationLog())
            }
        }
        list.sortedByDescending { it.timestamp }
    }

    FavoritesScreenContent(
        favoriteLogs = mergedFavorites,
        allLogs = allLogs,
        currentlyPlayingBarkPath = currentlyPlayingBarkPath,
        onToggleFavorite = { log -> viewModel.toggleFavorite(log) },
        onPlayBarkAudio = { path -> viewModel.playRecordedBark(path) },
        onStopBarkAudio = { viewModel.stopPlayingRecordedBark() },
        onSpeakTranslation = { text -> viewModel.speakTranslation(text) },
        onNavigateToTranslate = onNavigateToTranslate,
        modifier = modifier
    )
}

/**
 * Stateless, testable Compose layout for the Favorites navigation tab.
 */
@Composable
fun FavoritesScreenContent(
    favoriteLogs: List<TranslationLog>,
    allLogs: List<TranslationLog> = emptyList(),
    currentlyPlayingBarkPath: String? = null,
    onToggleFavorite: (TranslationLog) -> Unit = {},
    onPlayBarkAudio: (String) -> Unit = {},
    onStopBarkAudio: () -> Unit = {},
    onSpeakTranslation: (String) -> Unit = {},
    onNavigateToTranslate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDogFilter by remember { mutableStateOf("All") }
    var shareDialogLog by remember { mutableStateOf<TranslationLog?>(null) }
    var showPdfExportDialog by remember { mutableStateOf(false) }

    val availableDogNames = remember(favoriteLogs, allLogs) {
        (favoriteLogs.map { it.petName } + allLogs.map { it.petName })
            .filter { it.isNotBlank() }
            .distinct()
    }

    val filteredFavorites = remember(favoriteLogs, searchQuery, selectedDogFilter) {
        favoriteLogs
            .filter { log ->
                if (selectedDogFilter == "All") true
                else log.petName.equals(selectedDogFilter, ignoreCase = true)
            }
            .filter { log ->
                if (searchQuery.isBlank()) true
                else {
                    log.englishTranslation.contains(searchQuery, ignoreCase = true) ||
                        log.dogThought.contains(searchQuery, ignoreCase = true) ||
                        log.petName.contains(searchQuery, ignoreCase = true) ||
                        log.vocalizationType.contains(searchQuery, ignoreCase = true) ||
                        log.detectedEmotion.contains(searchQuery, ignoreCase = true)
                }
            }
    }

    val recentUnfavoritedLogs = remember(allLogs, favoriteLogs) {
        val favIds = favoriteLogs.map { it.id }.toSet()
        allLogs.filter { !it.isFavorite && it.id !in favIds }.take(6)
    }

    val dateFormatter = remember {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("favorites_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Favorite Bark Translations",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Quick access to your bookmarked canine translations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFB300).copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("favorites_count_badge")
                ) {
                    Text(
                        text = "⭐ ${favoriteLogs.size} Saved",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                if (favoriteLogs.isNotEmpty()) {
                    FilledTonalButton(
                        onClick = { showPdfExportDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("export_favorites_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export Favorites to PDF",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("PDF", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // 2. Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search favorite translations, dog name, or emotion...") },
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
                .testTag("favorites_search_input")
        )

        // 3. Dog Profile Filter Chips
        if (availableDogNames.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("favorites_dog_filter_row")
            ) {
                item {
                    FilterChip(
                        selected = selectedDogFilter == "All",
                        onClick = { selectedDogFilter = "All" },
                        label = { Text("All Dogs (${favoriteLogs.size})") },
                        modifier = Modifier.testTag("favorites_filter_all")
                    )
                }
                items(availableDogNames, key = { it }) { dogName ->
                    val countForDog = favoriteLogs.count { it.petName.equals(dogName, ignoreCase = true) }
                    FilterChip(
                        selected = selectedDogFilter.equals(dogName, ignoreCase = true),
                        onClick = { selectedDogFilter = dogName },
                        label = { Text("🐾 $dogName ($countForDog)") },
                        modifier = Modifier.testTag("favorites_filter_dog_$dogName")
                    )
                }
            }
        }

        // 4. Quick-Mark Recent Translations as Favorites Section
        if (recentUnfavoritedLogs.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_mark_favorites_section")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "✨ Tap a recent bark translation to mark as Favorite:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unfavorited_recent_row")
                    ) {
                        items(recentUnfavoritedLogs, key = { it.id }) { recentLog ->
                            AssistChip(
                                onClick = { onToggleFavorite(recentLog) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.StarBorder,
                                        contentDescription = "Mark as favorite",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "${recentLog.petName}: \"${recentLog.englishTranslation.take(28)}...\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("mark_as_favorite_chip_${recentLog.id}")
                            )
                        }
                    }
                }
            }
        }

        // 5. Favorites List or Empty State
        if (filteredFavorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .testTag("favorites_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFB300).copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Text(
                        text = if (searchQuery.isNotBlank() || selectedDogFilter != "All") {
                            "No matching favorite translations"
                        } else {
                            "No favorite bark translations yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Tap the ⭐ star icon on any bark translation or select a recent translation above to save it here for instant access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onNavigateToTranslate,
                        modifier = Modifier.testTag("empty_favorites_translate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Translate a Bark")
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
                    .testTag("favorites_list")
            ) {
                items(filteredFavorites, key = { it.id }) { log ->
                    val isPlayingBark = !log.audioFilePath.isNullOrBlank() &&
                        currentlyPlayingBarkPath == log.audioFilePath

                    FavoriteTranslationCard(
                        log = log,
                        dateString = dateFormatter.format(Date(log.timestamp)),
                        isPlayingBark = isPlayingBark,
                        onPlayBark = { path -> onPlayBarkAudio(path) },
                        onStopBark = onStopBarkAudio,
                        onSpeak = { onSpeakTranslation(log.englishTranslation) },
                        onQuickShare = {
                            SocialShareHelper.quickShareLatestTranslation(context, log)
                        },
                        onOpenShareDialog = { shareDialogLog = log },
                        onToggleFavorite = { onToggleFavorite(log) }
                    )
                }
            }
        }
    }

    if (showPdfExportDialog) {
        PdfExportDialog(
            logs = filteredFavorites,
            onDismissRequest = { showPdfExportDialog = false }
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
fun FavoriteTranslationCard(
    log: TranslationLog,
    dateString: String,
    isPlayingBark: Boolean,
    onPlayBark: (String) -> Unit,
    onStopBark: () -> Unit,
    onSpeak: () -> Unit,
    onQuickShare: () -> Unit,
    onOpenShareDialog: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgencyColor = when (log.urgencyLevel.uppercase()) {
        "CRITICAL" -> MaterialTheme.colorScheme.error
        "HIGH" -> MaterialTheme.colorScheme.tertiary
        "MEDIUM" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("favorite_card_${log.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.45f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Dog Profile + Urgency Badge + Star Favorite Toggle
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
                        text = "${log.petName} (${log.petBreed})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("toggle_favorite_button_${log.id}")
                    ) {
                        Icon(
                            imageVector = if (log.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (log.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = Color(0xFFFFB300)
                        )
                    }
                }
            }

            // Highlighted English Translation Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${log.englishTranslation}\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(12.dp)
                        .testTag("favorite_translation_text_${log.id}")
                )
            }

            // Inner Canine Thought
            if (log.dogThought.isNotBlank()) {
                Text(
                    text = "💭 ${log.dogThought}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Acoustic Telemetry & Action Buttons Row
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
                            text = "${log.dominantPitchHz}Hz • ${log.intensityDb}dB • $dateString",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!log.audioFilePath.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                if (isPlayingBark) onStopBark() else onPlayBark(log.audioFilePath)
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("play_favorite_audio_${log.id}")
                        ) {
                            Icon(
                                imageVector = if (isPlayingBark) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingBark) "Stop Bark Audio" else "Play Bark Audio",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("speak_favorite_button_${log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak favorite translation aloud",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }

                    IconButton(
                        onClick = onQuickShare,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("quick_share_favorite_button_${log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Quick Share Favorite Translation",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
