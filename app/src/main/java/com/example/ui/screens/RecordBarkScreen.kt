package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Delete
import com.example.audio.RecordedBarkSample
import com.example.data.PetProfile
import com.example.data.RecordedBark
import com.example.ui.BarkTalkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecordBarkScreen captures dog barks using the device microphone via MediaRecorder,
 * visualizes real-time acoustic levels, and extracts acoustic telemetry (frequency, dB, cadence)
 * to prepare the sample for AI translation and ethological analysis.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecordBarkScreen(
    viewModel: BarkTalkViewModel,
    onNavigateToTranslation: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToPastRecordings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val petProfiles by viewModel.petProfiles.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()
    val isRecording by viewModel.isMediaRecording.collectAsState()
    val durationSeconds by viewModel.mediaRecordingDurationSeconds.collectAsState()
    val volumeDb by viewModel.mediaRecorderVolumeDb.collectAsState()
    val waveform by viewModel.mediaRecorderWaveform.collectAsState()
    val lastRecordedSample by viewModel.lastRecordedSample.collectAsState()
    val currentlyPlayingBarkPath by viewModel.currentlyPlayingBarkPath.collectAsState()
    val recordedBarks by viewModel.recordedBarks.collectAsState()

    // Local selection state synced with Room's selectedPet so selecting a chip/dropdown item
    // immediately updates the recording's associated dog profile
    var locallyAssociatedPet by remember(selectedPet) {
        mutableStateOf(selectedPet)
    }
    val activeRecordingPet = locallyAssociatedPet ?: selectedPet ?: petProfiles.firstOrNull()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startMediaRecording()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("record_bark_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Acoustic Bark Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Record microphone samples and prepare acoustic telemetry for AI analysis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Dog Profile Selector Card (Dropdown + Selection Chips to associate recording with a dog profile)
        DogProfileRecordingSelector(
            profiles = petProfiles,
            selectedPet = activeRecordingPet,
            onSelectPet = { chosenPet ->
                locallyAssociatedPet = chosenPet
                if (chosenPet.id > 0L) {
                    viewModel.selectPet(chosenPet.id)
                }
            },
            onManageProfiles = onNavigateToProfiles
        )

        if (!hasMicPermission) {
            // Permission Prompt Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "Microphone Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "To record your dog's vocalizations, extract frequency pitch, and prepare samples for translation, BarkTalk needs microphone access.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("grant_mic_permission_button")
                    ) {
                        Text("Grant Microphone Access")
                    }
                }
            }
        }

        // Recording Studio Canvas Deck
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = if (isRecording) 2.dp else 1.dp,
                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isRecording) 6.dp else 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(
                                    color = if (isRecording) Color(0xFFE53935) else Color(0xFF43A047),
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = if (isRecording) "RECORDING LIVE AUDIO" else if (lastRecordedSample != null) "SAMPLE READY" else "STUDIO READY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Recording duration timer
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isRecording) {
                                String.format("%02d:%04.1fs", (durationSeconds / 60).toInt(), durationSeconds % 60)
                            } else if (lastRecordedSample != null) {
                                String.format("Sample: %.1fs", lastRecordedSample?.durationSeconds ?: 0f)
                            } else "00:00.0s",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Decibel Meter Progress Gauge
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
                            text = "Microphone Input Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${volumeDb.toInt()} dB",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (volumeDb > 70f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }

                    val progressRatio = ((volumeDb - 30f) / 70f).coerceIn(0.05f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = when {
                            volumeDb > 75f -> Color(0xFFE53935)
                            volumeDb > 65f -> Color(0xFFFFB300)
                            else -> Color(0xFF00897B)
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ambient (30dB)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text("🐾 Bark Threshold (70dB)", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        Text("100dB", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }

                // Dynamic Live Waveform Canvas
                DynamicBarkWaveform(
                    waveform = waveform,
                    isRecording = isRecording,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )

                // Giant Recording Action Button
                RecordButtonSection(
                    isRecording = isRecording,
                    hasPermission = hasMicPermission,
                    onRequestPermission = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onStartRecording = { viewModel.startMediaRecording() },
                    onStopRecording = { viewModel.stopMediaRecordingForPreparation() }
                )
            }
        }

        // PREPARED ANALYSIS DECK: Shown when audio sample has been captured
        AnimatedVisibility(
            visible = lastRecordedSample != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            lastRecordedSample?.let { sample ->
                PreparedAudioAnalysisCard(
                    sample = sample,
                    associatedPet = activeRecordingPet,
                    isPlaying = currentlyPlayingBarkPath == sample.audioFile.absolutePath,
                    onPlay = { viewModel.playRecordedBark(sample.audioFile.absolutePath) },
                    onStop = { viewModel.stopPlayingRecordedBark() },
                    onAnalyzeWithAi = {
                        viewModel.submitPreparedSampleForAnalysis(
                            sample = sample,
                            targetPet = activeRecordingPet
                        )
                        onNavigateToTranslation()
                    },
                    onReRecord = {
                        viewModel.discardRecordedSample()
                    }
                )
            }
        }

        // Stored Recorded Dog Barks Vault (Room Database Entity & DAO)
        RecordedBarksVaultSection(
            recordedBarks = recordedBarks,
            onSpeakTranslation = { viewModel.speakTranslation(it.translation) },
            onDeleteBark = { viewModel.deleteRecordedBark(it) },
            onViewAllRecordings = onNavigateToPastRecordings
        )
    }
}

/**
 * Interactive selector card on the recording screen that includes both a dropdown menu and
 * horizontal selection chips to associate a bark recording with a specific dog profile.
 */
@Composable
fun DogProfileRecordingSelector(
    profiles: List<PetProfile>,
    selectedPet: PetProfile?,
    onSelectPet: (PetProfile) -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayProfiles = remember(profiles, selectedPet) {
        if (profiles.isNotEmpty()) {
            profiles
        } else {
            listOf(
                selectedPet ?: PetProfile(
                    id = 1L,
                    name = "Barnaby",
                    breed = "Golden Retriever",
                    ageYears = 3,
                    avatarEmoji = "🦮",
                    personality = "Gentle Goofball",
                    isSelected = true
                ),
                PetProfile(
                    id = 2L,
                    name = "Luna",
                    breed = "French Bulldog",
                    ageYears = 2,
                    avatarEmoji = "🐕",
                    personality = "Dramatic Diva"
                ),
                PetProfile(
                    id = 3L,
                    name = "Zeus",
                    breed = "German Shepherd",
                    ageYears = 4,
                    avatarEmoji = "🐺",
                    personality = "Loyal Guardian"
                )
            )
        }
    }

    val currentPet = selectedPet ?: displayProfiles.first()
    var dropdownExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dog_profile_selector_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Dropdown Selector Button + Manage Profiles Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { dropdownExpanded = true }
                            .testTag("dog_profile_dropdown_button")
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
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    if (!currentPet.photoUri.isNullOrBlank()) {
                                        AsyncImage(
                                            model = currentPet.photoUri,
                                            contentDescription = "${currentPet.name}'s photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.clip(CircleShape)
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(currentPet.avatarEmoji, fontSize = 20.sp)
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Associate Recording With Dog:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${currentPet.name} (${currentPet.breed})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("selected_dog_profile_label")
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Dog Profile Dropdown",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.testTag("dog_profile_dropdown_menu")
                    ) {
                        displayProfiles.forEach { pet ->
                            val isSelected = pet.id == currentPet.id && pet.name == currentPet.name
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${pet.avatarEmoji} ${pet.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "${pet.breed} • ${pet.age} yrs",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected dog profile",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                onClick = {
                                    dropdownExpanded = false
                                    onSelectPet(pet)
                                },
                                modifier = Modifier.testTag("dog_profile_dropdown_item_${pet.id}")
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "+ Add or Manage Dog Profiles",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Pets,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                dropdownExpanded = false
                                onManageProfiles()
                            },
                            modifier = Modifier.testTag("dropdown_manage_dogs_item")
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                TextButton(
                    onClick = onManageProfiles,
                    modifier = Modifier.testTag("switch_target_dog_button")
                ) {
                    Text("Manage", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Bottom Row: Quick Selection Chips for each Dog Profile
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Quick-Select Dog Profile:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dog_profile_chips_row")
                ) {
                    items(displayProfiles, key = { "${it.id}_${it.name}" }) { pet ->
                        val isSelected = pet.id == currentPet.id && pet.name == currentPet.name
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPet(pet) },
                            label = {
                                Text(
                                    text = "${pet.avatarEmoji} ${pet.name}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("dog_profile_chip_${pet.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordButtonSection(
    isRecording: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecording) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Pulsing Ring
            if (isRecording) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulseScale)
                ) {}
            }

            Surface(
                shape = CircleShape,
                color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .clickable {
                        if (!hasPermission) {
                            onRequestPermission()
                        } else if (isRecording) {
                            onStopRecording()
                        } else {
                            onStartRecording()
                        }
                    }
                    .testTag("record_bark_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Text(
            text = if (isRecording) "Recording bark... Tap to Finish" else "Tap microphone to record dog bark",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DynamicBarkWaveform(
    waveform: FloatArray,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val activeColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val idleColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        val barCount = waveform.size
        val barSpacing = 4.dp.toPx()
        val totalSpacing = barSpacing * (barCount - 1)
        val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(2.dp.toPx())

        for (i in 0 until barCount) {
            val normalizedAmp = waveform.getOrElse(i) { 0.05f }.coerceIn(0.05f, 1.0f)
            val barHeight = (size.height * normalizedAmp).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + barSpacing)
            val y = (size.height - barHeight) / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isRecording) {
                        listOf(Color(0xFFE53935), Color(0xFFFF8A80))
                    } else {
                        listOf(activeColor, idleColor)
                    }
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/**
 * The Prepared Audio Analysis Deck: displays extracted telemetry, playback controls,
 * and preparation integrity metrics before AI submission.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreparedAudioAnalysisCard(
    sample: RecordedBarkSample,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onAnalyzeWithAi: () -> Unit,
    onReRecord: () -> Unit,
    associatedPet: PetProfile? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("prepared_audio_analysis_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Success badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Vocalization Prepared for Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = sample.clarityRating,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Associated Dog Profile Badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("associated_dog_profile_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(associatedPet?.avatarEmoji ?: "🐶", fontSize = 16.sp)
                    Text(
                        text = "Associated Dog Profile: ${associatedPet?.name ?: "Barnaby"} (${associatedPet?.breed ?: "Golden Retriever"})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Audio Player Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isPlaying) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { if (isPlaying) onStop() else onPlay() }
                                .testTag("play_recorded_sample_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Stop Audio" else "Play Audio",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Recorded Bark Audio",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AAC 44.1kHz • ${sample.fileSizeKb} KB • ${String.format("%.1fs", sample.durationSeconds)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (isPlaying) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Playing 🔊",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Extracted Acoustic Telemetry Grid
            Text(
                text = "Extracted Acoustic Features:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TelemetryPill(
                    icon = "🐾",
                    label = "Type",
                    value = sample.acousticAnalysis.vocalizationType
                )
                TelemetryPill(
                    icon = "🎵",
                    label = "Dominant Pitch",
                    value = "${sample.acousticAnalysis.pitchHz} Hz"
                )
                TelemetryPill(
                    icon = "🔊",
                    label = "Peak Intensity",
                    value = "${sample.peakDb} dB"
                )
                TelemetryPill(
                    icon = "⚡",
                    label = "Burst Count",
                    value = "${sample.detectedBursts} Pulses"
                )
                TelemetryPill(
                    icon = "⏱️",
                    label = "Cadence",
                    value = sample.acousticAnalysis.cadenceDescription
                )
                TelemetryPill(
                    icon = "📶",
                    label = "SNR Quality",
                    value = "+${sample.signalToNoiseRatioDb} dB SNR"
                )
            }

            // Preparation Checkmarks
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✓ Frequency spectra isolated (${sample.acousticAnalysis.pitchHz} Hz)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "✓ Decibel burst envelope analyzed (${sample.detectedBursts} distinct pulses)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "✓ Ready for Gemini AI canine semantic decoding",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onReRecord,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rerecord_bark_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Re-Record")
                }

                Button(
                    onClick = onAnalyzeWithAi,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("submit_for_analysis_button")
                ) {
                    Icon(Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Analyze with AI")
                }
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: String,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 14.sp)
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun RecordedBarksVaultSection(
    recordedBarks: List<RecordedBark>,
    onSpeakTranslation: (RecordedBark) -> Unit,
    onDeleteBark: (RecordedBark) -> Unit,
    onViewAllRecordings: () -> Unit = {}
) {
    val dateFormatter = remember {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recorded_barks_vault_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stored Recorded Dog Barks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Persisted in Room with recording timestamp & AI translation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${recordedBarks.size} Saved",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (recordedBarks.isEmpty()) {
                Text(
                    text = "No recorded barks saved yet. Record a dog bark above to store its timestamp and AI translation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                recordedBarks.take(5).forEach { bark ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recorded_bark_item_${bark.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🐾 ${bark.petName} • ${bark.vocalizationType}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormatter.format(Date(bark.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            Text(
                                text = "\"${bark.translation}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bark.peakDb} dB • ${bark.dominantPitchHz} Hz • ${bark.detectedEmotion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { onSpeakTranslation(bark) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("speak_recorded_bark_${bark.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Speak AI translation",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteBark(bark) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("delete_recorded_bark_${bark.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete recorded bark",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onViewAllRecordings,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_all_past_recordings_button")
            ) {
                Text("Open Full Past Recordings Screen")
            }
        }
    }
}

