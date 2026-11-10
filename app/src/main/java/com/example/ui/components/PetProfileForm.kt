package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PetProfile
import com.example.util.PhotoStorageHelper

/**
 * Jetpack Compose UI Form allowing users to input their dog's name, breed, age,
 * and pick a photo, saving the PetProfile to the local Room database.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetProfileForm(
    initialProfile: PetProfile? = null,
    onSaveProfile: (PetProfile) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initialProfile?.name ?: "") }
    var breed by remember { mutableStateOf(initialProfile?.breed ?: "") }
    var ageText by remember { mutableStateOf((initialProfile?.age ?: initialProfile?.ageYears ?: 3).toString()) }
    var photoUriString by remember { mutableStateOf(initialProfile?.photoUri) }
    var avatarEmoji by remember { mutableStateOf(initialProfile?.avatarEmoji ?: "🐶") }
    var personality by remember { mutableStateOf(initialProfile?.personality ?: "Gentle Goofball") }
    var favoriteTreat by remember { mutableStateOf(initialProfile?.favoriteTreat ?: "Cheddar Cheese") }

    var nameError by remember { mutableStateOf(false) }
    var breedError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }

    // Zero-permission Android Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = PhotoStorageHelper.savePhotoLocally(context, uri)
            photoUriString = localPath ?: uri.toString()
        }
    }

    val popularBreeds = listOf(
        "Golden Retriever", "French Bulldog", "German Shepherd",
        "Beagle", "Labrador", "Poodle", "Husky", "Corgi", "Dachshund", "Mixed Breed"
    )

    val emojis = listOf("🐶", "🐕", "🦮", "🐩", "🐺", "🐾", "🦊")
    val personalities = listOf("Gentle Goofball", "Loyal Guardian", "Excited Zoomer", "Dramatic Diva", "Foodie Connoisseur", "Anxious Sweetheart")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pet_profile_form"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialProfile != null) "Edit Dog Profile" else "Add Dog Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Customize AI translations and vocal recognition for your dog",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Photo Picker Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.size(100.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("pet_photo_picker_avatar")
                        ) {
                            if (!photoUriString.isNullOrBlank()) {
                                AsyncImage(
                                    model = photoUriString,
                                    contentDescription = "Dog's photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.clip(CircleShape)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(avatarEmoji, fontSize = 48.sp)
                                }
                            }
                        }

                        // Camera Badge
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Pick Photo",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.testTag("select_photo_button")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (photoUriString == null) "Pick Dog Photo" else "Change Photo")
                        }

                        if (photoUriString != null) {
                            IconButton(
                                onClick = { photoUriString = null },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove Photo",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Avatar Emoji fallback options
                    Text(
                        text = "Or choose avatar emoji:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        emojis.forEach { emoji ->
                            val isSelected = avatarEmoji == emoji
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { avatarEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Dog's Name Field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                label = { Text("Dog's Name *") },
                placeholder = { Text("e.g. Barnaby, Luna, Buster") },
                leadingIcon = {
                    Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name is required", color = MaterialTheme.colorScheme.error) }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_name_input")
            )

            // Breed Field with Quick Suggestions
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = breed,
                    onValueChange = {
                        breed = it
                        breedError = it.isBlank()
                    },
                    label = { Text("Breed *") },
                    placeholder = { Text("e.g. Golden Retriever, Beagle") },
                    isError = breedError,
                    supportingText = if (breedError) {
                        { Text("Breed is required", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_breed_input")
                )

                Text(
                    text = "Quick Select Breed:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    popularBreeds.take(6).forEach { b ->
                        FilterChip(
                            selected = breed.equals(b, ignoreCase = true),
                            onClick = {
                                breed = b
                                breedError = false
                            },
                            label = { Text(b, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Age Field (in Years)
            OutlinedTextField(
                value = ageText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    ageText = filtered
                    ageError = filtered.isBlank()
                },
                label = { Text("Age (Years) *") },
                placeholder = { Text("e.g. 3") },
                leadingIcon = {
                    Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                },
                isError = ageError,
                supportingText = if (ageError) {
                    { Text("Please enter a valid age", color = MaterialTheme.colorScheme.error) }
                } else {
                    { Text("Dog's age helps AI calibrate acoustic pitch decay") }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_age_input")
            )

            // Personality Archetype
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Canine Personality Archetype:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    personalities.forEach { p ->
                        val isSelected = personality == p
                        FilterChip(
                            selected = isSelected,
                            onClick = { personality = p },
                            label = { Text(p, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Favorite Treat
            OutlinedTextField(
                value = favoriteTreat,
                onValueChange = { favoriteTreat = it },
                label = { Text("Favorite Treat / Reward") },
                placeholder = { Text("e.g. Cheddar Cheese, Peanut Butter") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.testTag("cancel_pet_profile_button")
                ) {
                    Text("Cancel")
                }

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedBreed = breed.trim().ifEmpty { "Canine" }
                        val parsedAge = ageText.toIntOrNull() ?: 1

                        if (trimmedName.isBlank()) {
                            nameError = true
                            return@Button
                        }

                        val profileToSave = (initialProfile ?: PetProfile(
                            name = trimmedName,
                            breed = trimmedBreed,
                            age = parsedAge,
                            ageYears = parsedAge
                        )).copy(
                            name = trimmedName,
                            breed = trimmedBreed,
                            age = parsedAge,
                            ageYears = parsedAge,
                            photoUri = photoUriString,
                            avatarEmoji = avatarEmoji,
                            personality = personality,
                            favoriteTreat = favoriteTreat.trim().ifEmpty { "Treats" }
                        )

                        onSaveProfile(profileToSave)
                    },
                    modifier = Modifier.testTag("save_pet_profile_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (initialProfile != null) "Update Profile" else "Save Dog Profile")
                }
            }
        }
    }
}
