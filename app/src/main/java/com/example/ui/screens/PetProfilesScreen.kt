package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ai.AvatarGenerationState
import com.example.ai.PetAvatarGenerator
import com.example.data.PetProfile
import com.example.ui.BarkAdaptiveLayoutInfo
import com.example.ui.BarkTalkViewModel
import com.example.ui.components.PetProfileForm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PetProfilesScreen(
    viewModel: BarkTalkViewModel,
    modifier: Modifier = Modifier
) {
    val profiles by viewModel.petProfiles.collectAsStateWithLifecycle()
    val selectedPet by viewModel.selectedPet.collectAsStateWithLifecycle()
    val avatarGenState by viewModel.avatarGenState.collectAsStateWithLifecycle()

    PetProfilesAdaptiveContent(
        profiles = profiles,
        selectedPet = selectedPet,
        avatarGenState = avatarGenState,
        onSelectPet = { petId -> viewModel.selectPet(petId) },
        onAddPet = { newPet -> viewModel.addPetProfile(newPet) },
        onUpdatePet = { updatedPet -> viewModel.updatePetProfile(updatedPet) },
        onDeletePet = { pet -> viewModel.deletePetProfile(pet) },
        onGenerateAvatar = { pet, onSaved ->
            viewModel.generatePetAvatar(pet, onAvatarSaved = onSaved)
        },
        onResetAvatarState = { viewModel.resetAvatarGenerationState() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PetProfilesAdaptiveContent(
    profiles: List<PetProfile>,
    selectedPet: PetProfile?,
    avatarGenState: AvatarGenerationState = AvatarGenerationState.Idle,
    onSelectPet: (Long) -> Unit = {},
    onAddPet: (PetProfile) -> Unit = {},
    onUpdatePet: (PetProfile) -> Unit = {},
    onDeletePet: (PetProfile) -> Unit = {},
    onGenerateAvatar: (PetProfile, ((String) -> Unit)?) -> Unit = { _, _ -> },
    onResetAvatarState: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showProfileSheet by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<PetProfile?>(null) }
    var isAddingInDetailPane by remember { mutableStateOf(false) }
    var profileToDelete by remember { mutableStateOf<PetProfile?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("pet_profiles_screen")
    ) {
        val adaptiveInfo = remember(maxWidth, maxHeight) {
            BarkAdaptiveLayoutInfo.fromDimensions(maxWidth, maxHeight)
        }
        val isTwoPaneTablet = adaptiveInfo.useTwoPaneLayout

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        onResetAvatarState()
                        editingProfile = null
                        if (isTwoPaneTablet) {
                            isAddingInDetailPane = true
                        } else {
                            showProfileSheet = true
                        }
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
                    .padding(horizontal = adaptiveInfo.horizontalPadding, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Canine Pet Profiles",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add and manage your dogs, or generate bespoke Gemini AI avatars from their breed & personality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isTwoPaneTablet) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("tablet_list_detail_badge")
                        ) {
                            Text(
                                text = "Split List-Detail View",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Global AI Avatar Generation Banner when triggered from card list
                if (!showProfileSheet && avatarGenState !is AvatarGenerationState.Idle) {
                    Spacer(Modifier.height(12.dp))
                    AvatarGenerationStatusBanner(
                        state = avatarGenState,
                        onDismiss = onResetAvatarState
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (isTwoPaneTablet) {
                    // Canonical List-Detail Layout for Tablets & Unfolded Foldables
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("pet_profiles_tablet_two_pane"),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier
                                .weight(0.48f)
                                .fillMaxHeight()
                                .testTag("pet_profiles_list_pane")
                        ) {
                            items(profiles, key = { it.id }) { pet ->
                                val isSelected = pet.id == selectedPet?.id || pet.isSelected
                                PetProfileCardItem(
                                    pet = pet,
                                    isSelected = isSelected,
                                    canDelete = profiles.size > 1,
                                    avatarGenState = avatarGenState,
                                    onSelect = {
                                        isAddingInDetailPane = false
                                        editingProfile = pet
                                        onSelectPet(pet.id)
                                    },
                                    onEdit = {
                                        onResetAvatarState()
                                        isAddingInDetailPane = false
                                        editingProfile = pet
                                    },
                                    onDelete = { profileToDelete = pet },
                                    onGenerateAvatar = { onGenerateAvatar(pet, null) }
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.52f)
                                .fillMaxHeight()
                                .testTag("pet_profiles_detail_pane")
                        ) {
                            val activeDetailProfile = if (isAddingInDetailPane) {
                                null
                            } else {
                                editingProfile ?: selectedPet ?: profiles.firstOrNull()
                            }

                            PetProfileForm(
                                initialProfile = activeDetailProfile,
                                avatarGenState = avatarGenState,
                                onGenerateAiAvatar = { draftProfile, onGeneratedUri ->
                                    onGenerateAvatar(draftProfile, onGeneratedUri)
                                },
                                onDismissAvatarError = onResetAvatarState,
                                onSaveProfile = { updated ->
                                    if (activeDetailProfile != null) {
                                        onUpdatePet(updated)
                                        editingProfile = updated
                                    } else {
                                        onAddPet(updated)
                                        isAddingInDetailPane = false
                                    }
                                    onResetAvatarState()
                                },
                                onCancel = {
                                    isAddingInDetailPane = false
                                    editingProfile = selectedPet
                                    onResetAvatarState()
                                }
                            )
                        }
                    }
                } else {
                    // Compact Single-Pane List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 72.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("pet_profiles_list_pane")
                    ) {
                        items(profiles, key = { it.id }) { pet ->
                            val isSelected = pet.id == selectedPet?.id || pet.isSelected
                            PetProfileCardItem(
                                pet = pet,
                                isSelected = isSelected,
                                canDelete = profiles.size > 1,
                                avatarGenState = avatarGenState,
                                onSelect = { onSelectPet(pet.id) },
                                onEdit = {
                                    onResetAvatarState()
                                    editingProfile = pet
                                    showProfileSheet = true
                                },
                                onDelete = { profileToDelete = pet },
                                onGenerateAvatar = { onGenerateAvatar(pet, null) }
                            )
                        }
                    }
                }
            }
        }

        // Modal Sheet for Add / Edit on Compact screens
        if (showProfileSheet && !isTwoPaneTablet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            ModalBottomSheet(
                onDismissRequest = {
                    onResetAvatarState()
                    showProfileSheet = false
                },
                sheetState = sheetState
            ) {
                PetProfileForm(
                    initialProfile = editingProfile,
                    avatarGenState = avatarGenState,
                    onGenerateAiAvatar = { draftProfile, onGeneratedUri ->
                        onGenerateAvatar(draftProfile, onGeneratedUri)
                    },
                    onDismissAvatarError = onResetAvatarState,
                    onSaveProfile = { updated ->
                        if (editingProfile != null) {
                            onUpdatePet(updated)
                        } else {
                            onAddPet(updated)
                        }
                        onResetAvatarState()
                        showProfileSheet = false
                    },
                    onCancel = {
                        onResetAvatarState()
                        showProfileSheet = false
                    }
                )
            }
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
                        onDeletePet(pet)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PetProfileCardItem(
    pet: PetProfile,
    isSelected: Boolean,
    canDelete: Boolean,
    avatarGenState: AvatarGenerationState,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onGenerateAvatar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(52.dp)
                    ) {
                        if (!pet.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = pet.photoUri,
                                contentDescription = "${pet.name}'s photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape)
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Text(pet.avatarEmoji, fontSize = 28.sp)
                            }
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
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (canDelete) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(48.dp)
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

            // Tags: Personality, Pitch Profile, Treat, AI Avatar status
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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

                if (PetAvatarGenerator.isAiGeneratedAvatar(pet.photoUri)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "✨ AI Avatar",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Generate Unique Digital Avatar Action Button
            val isThisPetGenerating = (avatarGenState as? AvatarGenerationState.Generating)?.petId == pet.id
            val isAnyGenerating = avatarGenState is AvatarGenerationState.Generating
            FilledTonalButton(
                onClick = onGenerateAvatar,
                enabled = !isAnyGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("generate_avatar_pet_${pet.id}")
            ) {
                if (isThisPetGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Generating Gemini AI Avatar...")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generate unique AI digital avatar for ${pet.name}",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (PetAvatarGenerator.isAiGeneratedAvatar(pet.photoUri)) {
                            "Regenerate AI Digital Avatar"
                        } else {
                            "Generate Unique AI Digital Avatar"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarGenerationStatusBanner(
    state: AvatarGenerationState,
    onDismiss: () -> Unit
) {
    when (state) {
        is AvatarGenerationState.Generating -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profiles_avatar_loading_banner")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Designing ${state.petName}'s AI Avatar...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini is illustrating breed traits and personality (30–60s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        is AvatarGenerationState.Success -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profiles_avatar_success_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${state.petName}'s new Gemini AI avatar is saved!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            }
        }

        is AvatarGenerationState.Error -> {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.75f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profiles_avatar_error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column {
                            Text(
                                text = "AI Avatar Generation Failed",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }

        AvatarGenerationState.Idle -> Unit
    }
}

@Composable
fun PetProfileEditor(
    initialProfile: PetProfile?,
    onSave: (PetProfile) -> Unit,
    onCancel: () -> Unit
) {
    PetProfileForm(
        initialProfile = initialProfile,
        onSaveProfile = onSave,
        onCancel = onCancel
    )
}
