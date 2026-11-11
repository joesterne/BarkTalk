package com.example.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Mic
import com.example.R
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PetProfilesScreen
import com.example.ui.screens.RecordBarkScreen
import com.example.ui.screens.SoundboardScreen
import com.example.ui.screens.TranslateScreen
import com.example.widget.BarkTalkWidgetProvider
import kotlinx.coroutines.flow.StateFlow

enum class BarkNavTab(val title: String) {
    RECORD("Record"),
    TRANSLATE("Translate"),
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
    var currentTab by remember { mutableStateOf(BarkNavTab.TRANSLATE) }
    val selectedPet by viewModel.selectedPet.collectAsState()
    val incomingIntent by intentFlow?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(incomingIntent) {
        val targetTab = incomingIntent?.getStringExtra(BarkTalkWidgetProvider.EXTRA_START_TAB)
        if (targetTab == "soundboard") {
            currentTab = BarkNavTab.SOUNDBOARD
        } else if (incomingIntent?.getBooleanExtra(BarkTalkWidgetProvider.EXTRA_START_LISTENING, false) == true) {
            currentTab = BarkNavTab.TRANSLATE
            viewModel.startListening()
        }
    }

    // Back handling: if not on Translate tab, return to Translate tab
    BackHandler(enabled = currentTab != BarkNavTab.TRANSLATE) {
        currentTab = BarkNavTab.TRANSLATE
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🐕", fontSize = 20.sp)
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
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Gemini AI",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == BarkNavTab.RECORD,
                    onClick = { currentTab = BarkNavTab.RECORD },
                    icon = { Icon(Icons.Default.Mic, contentDescription = "Record Studio") },
                    label = { Text("Record") },
                    modifier = Modifier.testTag("nav_tab_record")
                )
                NavigationBarItem(
                    selected = currentTab == BarkNavTab.TRANSLATE,
                    onClick = { currentTab = BarkNavTab.TRANSLATE },
                    icon = { Icon(Icons.Default.Hearing, contentDescription = "Translate") },
                    label = { Text("Translate") },
                    modifier = Modifier.testTag("nav_tab_translate")
                )
                NavigationBarItem(
                    selected = currentTab == BarkNavTab.SOUNDBOARD,
                    onClick = { currentTab = BarkNavTab.SOUNDBOARD },
                    icon = { Icon(Icons.Default.VolumeUp, contentDescription = "Soundboard") },
                    label = { Text("Soundboard") },
                    modifier = Modifier.testTag("nav_tab_soundboard")
                )
                NavigationBarItem(
                    selected = currentTab == BarkNavTab.HISTORY,
                    onClick = { currentTab = BarkNavTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    modifier = Modifier.testTag("nav_tab_history")
                )
                NavigationBarItem(
                    selected = currentTab == BarkNavTab.PROFILES,
                    onClick = { currentTab = BarkNavTab.PROFILES },
                    icon = { Icon(Icons.Default.Pets, contentDescription = "Dogs") },
                    label = { Text("Dogs") },
                    modifier = Modifier.testTag("nav_tab_profiles")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                BarkNavTab.RECORD -> {
                    RecordBarkScreen(
                        viewModel = viewModel,
                        onNavigateToTranslation = { currentTab = BarkNavTab.TRANSLATE },
                        onNavigateToProfiles = { currentTab = BarkNavTab.PROFILES }
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
                BarkNavTab.SOUNDBOARD -> {
                    SoundboardScreen(viewModel = viewModel)
                }
                BarkNavTab.HISTORY -> {
                    HistoryScreen(viewModel = viewModel)
                }
                BarkNavTab.PROFILES -> {
                    PetProfilesScreen(viewModel = viewModel)
                }
            }
        }
    }
}
