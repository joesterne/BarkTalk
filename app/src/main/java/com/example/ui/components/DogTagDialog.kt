package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object DogTagPresets {
    val DEFAULT_TAGS: List<String> = listOf(
        "Squirrel Patrol",
        "Doorbell Alert",
        "Treat Time",
        "Walk Time",
        "Playtime",
        "Dinner Bell",
        "Backyard Guard",
        "Good Pup"
    )
}

/**
 * Interactive pill displaying a bark log's optional user-assigned dog tag (or an "+ Add Dog Tag"
 * prompt when unassigned). Enforces a 48dp minimum touch height for accessibility.
 */
@Composable
fun DogTagBadge(
    dogTag: String?,
    onClick: () -> Unit,
    testTagName: String,
    modifier: Modifier = Modifier
) {
    val hasTag = !dogTag.isNullOrBlank()
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTagName)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (hasTag) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (hasTag) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = if (hasTag) Icons.AutoMirrored.Filled.Label else Icons.Default.Add,
                    contentDescription = if (hasTag) "Edit dog tag $dogTag" else "Assign optional dog tag",
                    tint = if (hasTag) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (hasTag) "#$dogTag" else "Add Dog Tag",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (hasTag) FontWeight.Bold else FontWeight.Medium,
                    color = if (hasTag) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (hasTag) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

/**
 * Material 3 dialog allowing the user to assign, edit, or clear an optional dog tag
 * on any translated bark log stored in the Room database.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DogTagEditorDialog(
    initialTag: String?,
    translatedTextPreview: String,
    suggestedTags: List<String> = emptyList(),
    onSaveTag: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var tagInput by remember(initialTag) { mutableStateOf(initialTag.orEmpty()) }
    val allSuggestions = remember(suggestedTags) {
        (suggestedTags + DogTagPresets.DEFAULT_TAGS)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
            .take(10)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dog_tag_editor_dialog"),
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Label,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (initialTag.isNullOrBlank()) "Assign Dog Tag" else "Edit Dog Tag",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$translatedTextPreview\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { if (it.length <= 32) tagInput = it },
                    label = { Text("Optional Dog Tag") },
                    placeholder = { Text("e.g. Squirrel Patrol, Treat Time, Barnaby") },
                    singleLine = true,
                    trailingIcon = {
                        if (tagInput.isNotEmpty()) {
                            IconButton(
                                onClick = { tagInput = "" },
                                modifier = Modifier.testTag("dog_tag_clear_input_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear dog tag input"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dog_tag_input")
                )

                Text(
                    text = "Quick Tags:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    allSuggestions.forEach { preset ->
                        SuggestionChip(
                            onClick = { tagInput = preset },
                            label = {
                                Text(
                                    text = "#$preset",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.testTag("dog_tag_preset_${preset.lowercase().replace(" ", "_")}")
                        )
                    }
                }

                if (!initialTag.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    TextButton(
                        onClick = { onSaveTag(null) },
                        modifier = Modifier.testTag("remove_dog_tag_button")
                    ) {
                        Text(
                            text = "Remove Dog Tag (Leave Untagged)",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleaned = tagInput.trim().takeIf { it.isNotEmpty() }
                    onSaveTag(cleaned)
                },
                modifier = Modifier.testTag("save_dog_tag_button")
            ) {
                Text("Save Tag")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_dog_tag_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
