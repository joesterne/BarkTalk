package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PetProfile
import com.example.ui.BarkTalkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetProfilesScreen(
    viewModel: BarkTalkViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.petProfiles.collectAsState()
    val selectedPet by viewModel.selectedPet.collectAsState()

    var showProfileSheet by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<PetProfile?>(null) }
    var profileToDelete by remember { mutableStateOf<PetProfile?>(null) }

    Scaffold(
        modifier = modifier.testTag("pet_profiles_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingProfile = null
                    showProfileSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_pet_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Pet Profile")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Canine Pet Profiles",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add and manage your dogs. AI customizes translation nuance and personality based on the active dog.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 72.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(profiles, key = { it.id }) { pet ->
                    val isSelected = pet.id == selectedPet?.id || pet.isSelected

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectPet(pet.id) }
                            .testTag("pet_profile_${pet.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.size(52.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(pet.avatarEmoji, fontSize = 28.sp)
                                        }
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = pet.name,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isSelected) {
                                                Spacer(Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                ) {
                                                    Text(
                                                        text = "ACTIVE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${pet.breed} • ${pet.ageYears}y ${pet.ageMonths}m",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingProfile = pet
                                            showProfileSheet = true
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Profile",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (profiles.size > 1) {
                                        IconButton(
                                            onClick = { profileToDelete = pet },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Profile",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Tags: Personality, Pitch Profile, Treat
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "🎭 ${pet.personality}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "🔊 ${pet.pitchProfile}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = "🍖 ${pet.favoriteTreat}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet for Add / Edit
    if (showProfileSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            sheetState = sheetState
        ) {
            PetProfileEditor(
                initialProfile = editingProfile,
                onSave = { updated ->
                    if (editingProfile != null) {
                        viewModel.updatePetProfile(updated)
                    } else {
                        viewModel.addPetProfile(updated)
                    }
                    showProfileSheet = false
                },
                onCancel = { showProfileSheet = false }
            )
        }
    }

    // Confirm Delete Dialog
    profileToDelete?.let { pet ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            title = { Text("Delete ${pet.name}?") },
            text = { Text("Are you sure you want to remove ${pet.name}'s profile? Saved translations will remain in your history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePetProfile(pet)
                        profileToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PetProfileEditor(
    initialProfile: PetProfile?,
    onSave: (PetProfile) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var breed by remember { mutableStateOf(initialProfile?.breed ?: "Golden Retriever") }
    var ageYearsText by remember { mutableStateOf((initialProfile?.ageYears ?: 3).toString()) }
    var ageMonthsText by remember { mutableStateOf((initialProfile?.ageMonths ?: 0).toString()) }
    var avatarEmoji by remember { mutableStateOf(initialProfile?.avatarEmoji ?: "🐶") }
    var personality by remember { mutableStateOf(initialProfile?.personality ?: "Gentle Goofball") }
    var pitchProfile by remember { mutableStateOf(initialProfile?.pitchProfile ?: "Standard Bark") }
    var favoriteTreat by remember { mutableStateOf(initialProfile?.favoriteTreat ?: "Cheddar Cheese") }

    val emojis = listOf("🐶", "🐕", "🦮", "🐩", "🐺", "🐾", "🦊")
    val personalities = listOf("Gentle Goofball", "Loyal Guardian", "Excited Zoomer", "Dramatic Diva", "Foodie Connoisseur", "Anxious Sweetheart")
    val pitchProfiles = listOf("High Yip", "Standard Bark", "Deep Woof")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (initialProfile != null) "Edit Pet Profile" else "Add New Dog Profile",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        // Emoji Avatar selector
        Text("Choose Dog Avatar Emoji:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(emojis) { emoji ->
                Surface(
                    shape = CircleShape,
                    color = if (avatarEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (avatarEmoji == emoji) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { avatarEmoji = emoji }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, fontSize = 24.sp)
                    }
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Dog's Name *") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_pet_name"),
            singleLine = true
        )

        OutlinedTextField(
            value = breed,
            onValueChange = { breed = it },
            label = { Text("Breed * (e.g. Beagle, German Shepherd, Corgi)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = ageYearsText,
                onValueChange = { ageYearsText = it.filter { c -> c.isDigit() } },
                label = { Text("Age (Years)") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = ageMonthsText,
                onValueChange = { ageMonthsText = it.filter { c -> c.isDigit() } },
                label = { Text("Months") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Personality
        Text("Canine Personality Archetype:", style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(personalities) { p ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (personality == p) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { personality = p }
                ) {
                    Text(
                        text = p,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (personality == p) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Pitch Profile
        Text("Bark Vocal Pitch:", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pitchProfiles.forEach { p ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (pitchProfile == p) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { pitchProfile = p }
                ) {
                    Text(
                        text = p,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pitchProfile == p) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = favoriteTreat,
            onValueChange = { favoriteTreat = it },
            label = { Text("Favorite Treat / Toy") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val years = ageYearsText.toIntOrNull() ?: 1
                        val months = (ageMonthsText.toIntOrNull() ?: 0).coerceIn(0, 11)
                        val profile = (initialProfile ?: PetProfile(
                            name = name.trim(),
                            breed = breed.trim().ifEmpty { "Canine" },
                            ageYears = years,
                            ageMonths = months
                        )).copy(
                            name = name.trim(),
                            breed = breed.trim().ifEmpty { "Canine" },
                            ageYears = years,
                            ageMonths = months,
                            avatarEmoji = avatarEmoji,
                            personality = personality,
                            pitchProfile = pitchProfile,
                            favoriteTreat = favoriteTreat.trim().ifEmpty { "Treats" }
                        )
                        onSave(profile)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_pet_button")
            ) {
                Text("Save Dog Profile")
            }
        }
    }
}
