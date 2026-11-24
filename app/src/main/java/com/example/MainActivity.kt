package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebase.configureAppCheck
import com.example.ui.BarkTalkApp
import com.example.ui.BarkTalkViewModel
import com.example.ui.theme.BarkTalkTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val viewModel: BarkTalkViewModel by viewModels()
    private val currentIntentFlow = MutableStateFlow<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        currentIntentFlow.value = intent

        enableEdgeToEdge()

        setContent {
            val selectedPalette by viewModel.selectedColorPalette.collectAsStateWithLifecycle()
            val selectedDarkMode by viewModel.selectedDarkMode.collectAsStateWithLifecycle()

            BarkTalkTheme(
                colorPalette = selectedPalette,
                darkMode = selectedDarkMode
            ) {
                BarkTalkApp(
                    viewModel = viewModel,
                    intentFlow = currentIntentFlow
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentFlow.value = intent
    }
}
