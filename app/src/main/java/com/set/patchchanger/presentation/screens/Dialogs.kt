package com.set.patchchanger.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.DisplayNameType
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.presentation.viewmodel.MainUiState
import com.set.patchchanger.ui.theme.getModxColors

/**
 * Generic confirmation dialog
 */
@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog to edit Bank and Page names
 */
@Composable
fun BankPageNameDialog(
    state: MainUiState.Success,
    onDismiss: () -> Unit,
    onSaveBank: (String) -> Unit,
    onSavePage: (String) -> Unit
) {
    var bankName by remember { mutableStateOf(state.patchData.bankNames[state.settings.currentBankIndex]) }
    var pageName by remember { mutableStateOf(state.patchData.pageNames[state.settings.currentPageIndex]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Bank/Page Names", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pageName,
                    onValueChange = { pageName = it },
                    label = { Text("Page Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSaveBank(bankName)
                        onSavePage(pageName)
                        onDismiss()
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to edit a Sample Pad (S1-S4)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSampleDialog(
    sample: SamplePad,
    onDismiss: () -> Unit,
    onSave: (SamplePad) -> Unit,
    onLoadFile: () -> Unit,
    onSelectFromLibrary: () -> Unit,
    onClearAudio: () -> Unit,
    onEditColor: () -> Unit
) {
    var name by remember(sample.name) { mutableStateOf(sample.name) }
    var volume by remember(sample.volume) { mutableStateOf(sample.volume.toFloat()) }
    var loop by remember(sample.loop) { mutableStateOf(sample.loop) }
    val buttonColor = try {
        Color(android.graphics.Color.parseColor(sample.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Sample: $name", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Button Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Text("Audio Source", fontWeight = FontWeight.Bold)
                Text(
                    "Current: ${sample.sourceName ?: "None"}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = onLoadFile) { Text("Load New File") }
                    Button(onClick = onSelectFromLibrary) { Text("Select from Library") }
                }
                Spacer(Modifier.height(16.dp))

                Text("Button Color", fontWeight = FontWeight.Bold)
                Button(
                    onClick = onEditColor,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Color")
                }
                Spacer(Modifier.height(16.dp))

                Text("Volume: ${volume.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0f..100f,
                    steps = 99
                )
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = loop, onCheckedChange = { loop = it })
                    Text("Loop Playback")
                }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onClearAudio,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Audio & Reset Name")
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            sample.copy(
                                name = name,
                                volume = volume.toInt(),
                                loop = loop
                            )
                        )
                        onDismiss()
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to select a color from a grid
 */
@Composable
fun ColorPickerDialog(
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    val colors = getModxColors()

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
            ) {
                Text("Select Color", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(colors) { color ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .background(
                                    Color(android.graphics.Color.parseColor(color.hex)),
                                    CircleShape
                                )
                                .clickable { onColorSelected(color.hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            // Optional: Show color name
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Dialog to select audio from the library
 */
@Composable
fun AudioLibraryDialog(
    library: List<AudioLibraryItem>,
    onDismiss: () -> Unit,
    onSelect: (AudioLibraryItem) -> Unit,
    onDelete: (AudioLibraryItem) -> Unit,
    onAddFile: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<AudioLibraryItem?>(null) }
    val filteredList = library.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxHeight(0.8f)) {
            Column(
                Modifier
                    .padding(16.dp)
            ) {
                Text("Select Audio from Library", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by name...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    items(filteredList) { item ->
                        Text(
                            text = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item }
                                .background(
                                    if (selectedItem == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onAddFile) { Text("Add File...") }
                    Button(
                        onClick = { selectedItem?.let { onDelete(it); selectedItem = null } },
                        enabled = selectedItem != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Selected")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { selectedItem?.let { onSelect(it); onDismiss() } },
                        enabled = selectedItem != null
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

/**
 * Dialog to edit a Patch Slot (the main grid item)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotDialog(
    slot: PatchSlot,
    samples: List<SamplePad>,
    onDismiss: () -> Unit,
    onSave: (PatchSlot) -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onSwap: () -> Unit,
    onClear: () -> Unit
) {
    var name by remember(slot.name) { mutableStateOf(slot.name) }
    var displayNameType by remember(slot.displayNameType) { mutableStateOf(slot.displayNameType) }
    var assignedSample by remember(slot.assignedSample) { mutableStateOf(slot.assignedSample) }
    var colorHex by remember(slot.color) { mutableStateOf(slot.color) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showPerfBrowser by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Edit Slot: ${slot.getDisplayName()}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Slot Custom Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Display on Main Grid:")
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = displayNameType == DisplayNameType.PERFORMANCE,
                            onClick = { displayNameType = DisplayNameType.PERFORMANCE })
                        Text("Performance Name")
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = displayNameType == DisplayNameType.CUSTOM,
                            onClick = { displayNameType = DisplayNameType.CUSTOM })
                        Text("Custom")
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Assign Performance
                Text("Assigned Performance", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = slot.performanceName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Performance") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPerfBrowser = true },
                    enabled = false // Make it look clickable but not editable
                )
                Spacer(Modifier.height(16.dp))

                // Assign Sample Pad
                Text("Assign Sample Pad", fontWeight = FontWeight.Bold)
                var sampleDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sampleDropdownExpanded,
                    onExpandedChange = { sampleDropdownExpanded = !sampleDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = samples.find { it.id == assignedSample }?.name ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sampleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = sampleDropdownExpanded,
                        onDismissRequest = { sampleDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                assignedSample = -1
                                sampleDropdownExpanded = false
                            }
                        )
                        samples.forEach { sample ->
                            DropdownMenuItem(
                                text = { Text(sample.name) },
                                onClick = {
                                    assignedSample = sample.id
                                    sampleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Slot Actions
                Text("Slot Actions", fontWeight = FontWeight.Bold)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { showColorPicker = true }) { Text("Color") }
                    Button(onClick = { onCopy(); onDismiss() }) { Text("Copy") }
                    Button(onClick = { onPaste(); onDismiss() }) { Text("Paste") }
                    Button(onClick = { onSwap(); onDismiss() }) { Text("Swap") }
                }
                Button(
                    onClick = { onClear(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Clear")
                }
                Spacer(Modifier.height(16.dp))

                // Save/Cancel
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            slot.copy(
                                name = name,
                                displayNameType = displayNameType,
                                assignedSample = assignedSample,
                                color = colorHex
                                // TODO: Update MSB/LSB/PC/PerfName from Perf Browser
                            )
                        )
                        onDismiss()
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            onDismiss = { showColorPicker = false },
            onColorSelected = { colorHex = it; showColorPicker = false }
        )
    }

    if (showPerfBrowser) {
        // TODO: Implement Performance Browser Dialog
        // This is a complex dialog shown in the video, skipping for brevity
        // For now, just a placeholder dialog
        AlertDialog(
            onDismissRequest = { showPerfBrowser = false },
            title = { Text("Performance Browser") },
            text = { Text("This would show the performance list.") },
            confirmButton = { Button(onClick = { showPerfBrowser = false }) { Text("OK") } }
        )
    }
}

/**
 * Dialog to select another slot to swap with
 */
@Composable
fun SwapDialog(
    currentPageSlots: List<PatchSlot>,
    sourceSlot: PatchSlot,
    onDismiss: () -> Unit,
    onSelectSlot: (PatchSlot) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Swap \"${sourceSlot.getDisplayName()}\" with...",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(currentPageSlots.filter { it.id != sourceSlot.id }) { slot ->
                        val bgColor = try {
                            Color(android.graphics.Color.parseColor(slot.color))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier
                                .aspectRatio(1.618f)
                                .clickable { onSelectSlot(slot) },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(4.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot.getDisplayName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}