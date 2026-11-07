package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audio.CanineSoundboardPlayer
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.CanineEmotionSoundboard

@Composable
fun SoundboardScreen(
    viewModel: BarkTalkViewModel,
    modifier: Modifier = Modifier
) {
    val currentlyPlayingId by viewModel.currentlyPlayingSoundId.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("soundboard_screen")
    ) {
        // Header
        Text(
            text = "Two-Way Canine Soundboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Synthesized barks mapped to canine emotions (Happy, Playful, Warning, Alert, Curious, Calm) played via the device speaker.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        // Emotion-Mapped Soundboard Component
        CanineEmotionSoundboard(
            items = CanineSoundboardPlayer.SOUNDBOARD_ITEMS,
            currentlyPlayingId = currentlyPlayingId,
            onPlaySound = { item -> viewModel.playSoundboard(item) },
            onStopSound = { viewModel.stopSoundboard() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
