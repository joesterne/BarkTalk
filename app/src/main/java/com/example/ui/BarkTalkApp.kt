package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EasterEggUnlockedDialog
import com.example.ui.components.EasterEggVaultDialog
import com.example.ui.components.ThemePickerDialog
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PastRecordingsScreen
import com.example.ui.screens.PetProfilesScreen
import com.example.ui.screens.RecordBarkScreen
import com.example.ui.screens.SoundboardScreen
import com.example.ui.screens.TranslateScreen
import com.example.util.BarkEasterEgg
import com.example.util.BarkHistoryPdfExporter.toTranslationLog
import com.example.util.SocialShareHelper
import com.example.widget.BarkTalkWidgetProvider
import kotlinx.coroutines.flow.StateFlow

enum class BarkNavTab(val title: String) {
    RECORD("Record"),
    TRANSLATE("Translate"),
    FAVORITES("Favorites"),
    SOUNDBOARD("Soundboard"),
    HISTORY("History"),
    PROFILES("Dogs")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarkTalkApp(
    viewModel: BarkTalkViewModel,
    intentFlow: StateFlow<Intent?>? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(BarkNavTab.TRANSLATE) }
    var showPastRecordings by remember { mutableStateOf(false) }
    var showEasterEggVault by remember { mutableStateOf(false) }
    var showThemePicker by remember { mutableStateOf(false) }
    val selectedPet by viewModel.selectedPet.collectAsStateWithLifecycle()
    val translationLogs by viewModel.translationLogs.collectAsStateWithLifecycle()
    val recordedBarks by viewModel.recordedBarks.collectAsStateWithLifecycle()
    val unlockedEasterEggs by viewModel.unlockedEasterEggs.collectAsStateWithLifecycle()
    val zoomiesModeEnabled by viewModel.zoomiesModeEnabled.collectAsStateWithLifecycle()
    val activeEasterEggEvent by viewModel.activeEasterEggEvent.collectAsStateWithLifecycle()
    val snootBoopCount by viewModel.snootBoopCount.collectAsStateWithLifecycle()
    val selectedColorPalette by viewModel.selectedColorPalette.collectAsStateWithLifecycle()
    val selectedDarkMode by viewModel.selectedDarkMode.collectAsStateWithLifecycle()
    val incomingIntent by intentFlow?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) }

    LaunchedEffect(incomingIntent) {
        val targetTab = incomingIntent?.getStringExtra(BarkTalkWidgetProvider.EXTRA_START_TAB)
        if (targetTab == "soundboard") {
            showPastRecordings = false
            currentTab = BarkNavTab.SOUNDBOARD
        } else if (incomingIntent?.getBooleanExtra(BarkTalkWidgetProvider.EXTRA_START_LISTENING, false) == true) {
            showPastRecordings = false
            currentTab = BarkNavTab.TRANSLATE
            viewModel.startListening()
        }
    }

    // Back handling: if viewing PastRecordingsScreen or not on Translate tab
    BackHandler(enabled = showPastRecordings || currentTab != BarkNavTab.TRANSLATE) {
        if (showPastRecordings) {
            showPastRecordings = false
        } else {
            currentTab = BarkNavTab.TRANSLATE
        }
    }

    ProvideAdaptiveLayout(modifier = modifier) { adaptiveInfo ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable { viewModel.onTopBarSnootBoop() }
                                    .testTag("top_bar_snoot_boop_logo")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (zoomiesModeEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = when {
                                                zoomiesModeEnabled -> "⚡"
                                                snootBoopCount >= 3 -> "🐾"
                                                else -> "🐕"
                                            },
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "BarkTalk",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (zoomiesModeEnabled) {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                Text(
                                    text = if (zoomiesModeEnabled) "⚡ Zoomies" else "Gemini AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (zoomiesModeEnabled) {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (adaptiveInfo.isTabletOrExpanded) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.testTag("adaptive_window_size_badge")
                                ) {
                                    Text(
                                        text = adaptiveInfo.windowSizeClass.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showThemePicker = true },
                            modifier = Modifier.testTag("top_bar_theme_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Change App Color Theme",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (unlockedEasterEggs.isNotEmpty()) {
                            IconButton(
                                onClick = { showEasterEggVault = true },
                                modifier = Modifier.testTag("top_bar_easter_egg_vault_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Open Secret Easter Egg Vault (${unlockedEasterEggs.size}/${BarkEasterEgg.entries.size})",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                val latest = translationLogs.firstOrNull()
                                    ?: recordedBarks.firstOrNull()?.toTranslationLog()
                                SocialShareHelper.quickShareLatestTranslation(context, latest)
                            },
                            modifier = Modifier.testTag("top_bar_quick_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Quick Share Latest Bark Translation",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { showPastRecordings = !showPastRecordings },
                            modifier = Modifier.testTag("open_past_recordings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = "Past Bark Recordings",
                                tint = if (showPastRecordings) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!adaptiveInfo.useNavigationRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.testTag("bottom_navigation_bar")
                    ) {
                        NavigationBarItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.RECORD,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.RECORD
                            },
                            icon = { Icon(Icons.Default.Mic, contentDescription = "Record Studio") },
                            label = { Text("Record", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_record")
                        )
                        NavigationBarItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.TRANSLATE,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.TRANSLATE
                            },
                            icon = { Icon(Icons.Default.Hearing, contentDescription = "Translate") },
                            label = { Text("Translate", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_translate")
                        )
                        NavigationBarItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.FAVORITES,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.FAVORITES
                            },
                            icon = { Icon(Icons.Default.Star, contentDescription = "Favorites") },
                            label = { Text("Favorites", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )
                        NavigationBarItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.SOUNDBOARD,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.SOUNDBOARD
                            },
                            icon = { Icon(Icons.Default.VolumeUp, contentDescription = "Soundboard") },
                            label = { Text("Sounds", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_soundboard")
                        )
                        NavigationBarItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.HISTORY,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.HISTORY
                            },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_history")
                        )
                        NavigationBarItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.PROFILES,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.PROFILES
                            },
                            icon = { Icon(Icons.Default.Pets, contentDescription = "Dogs") },
                            label = { Text("Dogs", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_profiles")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (adaptiveInfo.useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedPet?.avatarEmoji ?: "🐶",
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .testTag("tablet_navigation_rail")
                    ) {
                        Spacer(Modifier.height(4.dp))
                        NavigationRailItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.RECORD,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.RECORD
                            },
                            icon = { Icon(Icons.Default.Mic, contentDescription = "Record Studio") },
                            label = { Text("Record", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_record")
                        )
                        NavigationRailItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.TRANSLATE,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.TRANSLATE
                            },
                            icon = { Icon(Icons.Default.Hearing, contentDescription = "Translate") },
                            label = { Text("Translate", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_translate")
                        )
                        NavigationRailItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.FAVORITES,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.FAVORITES
                            },
                            icon = { Icon(Icons.Default.Star, contentDescription = "Favorites") },
                            label = { Text("Favorites", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_favorites")
                        )
                        NavigationRailItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.SOUNDBOARD,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.SOUNDBOARD
                            },
                            icon = { Icon(Icons.Default.VolumeUp, contentDescription = "Soundboard") },
                            label = { Text("Sounds", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_soundboard")
                        )
                        NavigationRailItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.HISTORY,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.HISTORY
                            },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_history")
                        )
                        NavigationRailItem(
                            selected = !showPastRecordings && currentTab == BarkNavTab.PROFILES,
                            onClick = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.PROFILES
                            },
                            icon = { Icon(Icons.Default.Pets, contentDescription = "Dogs") },
                            label = { Text("Dogs", maxLines = 1) },
                            modifier = Modifier.testTag("nav_tab_profiles")
                        )
                    }

                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (showPastRecordings) {
                        PastRecordingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { showPastRecordings = false },
                            onNavigateToRecord = {
                                showPastRecordings = false
                                currentTab = BarkNavTab.RECORD
                            }
                        )
                    } else {
                        when (currentTab) {
                            BarkNavTab.RECORD -> {
                                RecordBarkScreen(
                                    viewModel = viewModel,
                                    onNavigateToTranslation = { currentTab = BarkNavTab.TRANSLATE },
                                    onNavigateToProfiles = { currentTab = BarkNavTab.PROFILES },
                                    onNavigateToPastRecordings = { showPastRecordings = true }
                                )
                            }
                            BarkNavTab.TRANSLATE -> {
                                TranslateScreen(
                                    viewModel = viewModel,
                                    onNavigateToProfiles = { currentTab = BarkNavTab.PROFILES },
                                    onNavigateToRecord = { currentTab = BarkNavTab.RECORD },
                                    onNavigateToSoundboard = { currentTab = BarkNavTab.SOUNDBOARD }
                                )
                            }
                            BarkNavTab.FAVORITES -> {
                                FavoritesScreen(
                                    viewModel = viewModel,
                                    onNavigateToTranslate = { currentTab = BarkNavTab.TRANSLATE }
                                )
                            }
                            BarkNavTab.SOUNDBOARD -> {
                                SoundboardScreen(viewModel = viewModel)
                            }
                            BarkNavTab.HISTORY -> {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    onNavigateToRecord = { currentTab = BarkNavTab.RECORD }
                                )
                            }
                            BarkNavTab.PROFILES -> {
                                PetProfilesScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }

        // Easter Egg Unlock Celebration Overlay & Confetti
        activeEasterEggEvent?.let { event ->
            EasterEggUnlockedDialog(
                event = event,
                unlockedCount = unlockedEasterEggs.size,
                totalCount = BarkEasterEgg.entries.size,
                onDismiss = { viewModel.dismissEasterEggEvent() },
                onOpenVault = { showEasterEggVault = true }
            )
        }

        // Secret Good Boy Vault Dialog
        if (showEasterEggVault) {
            EasterEggVaultDialog(
                unlockedEggs = unlockedEasterEggs,
                zoomiesModeEnabled = zoomiesModeEnabled,
                onToggleZoomiesMode = { viewModel.toggleZoomiesMode() },
                onDismiss = { showEasterEggVault = false }
            )
        }

        // Color Theme & Appearance Picker Dialog
        if (showThemePicker) {
            ThemePickerDialog(
                selectedPalette = selectedColorPalette,
                selectedDarkMode = selectedDarkMode,
                onSelectPalette = { palette -> viewModel.setColorPalette(palette) },
                onSelectDarkMode = { mode -> viewModel.setThemeDarkMode(mode) },
                onDismiss = { showThemePicker = false }
            )
        }
    }
}
