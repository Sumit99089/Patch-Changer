package com.set.patchchanger.presentation.screens

import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.DisplayNameType
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.getModxColors

// --- SHARED COMPONENTS FOR POLISH ---
@Composable
fun DialogHeader(title: String, onClose: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp), // Compact vertical padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (onClose != null) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(24.dp) // Smaller close button touch target
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
            }
        }
    }
    HorizontalDivider()
}

@Composable
fun DialogFooter(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    saveLabel: String = "Save",
    showCancel: Boolean = true
) {
    HorizontalDivider()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp), // Compact footer
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showCancel) {
            TextButton(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.width(8.dp))
        }
        Button(onClick = onSave) { Text(saveLabel) }
    }
}

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun BankPageNameDialog(
    state: MainUiState.Success,
    onDismiss: () -> Unit,
    onSaveBank: (String) -> Unit,
    onSavePage: (String) -> Unit
) {
    var bankName by remember { mutableStateOf(state.patchData.bankNames.getOrElse(state.settings.currentBankIndex) { "User" }) }
    var pageName by remember { mutableStateOf(state.patchData.pageNames.getOrElse(state.settings.currentPageIndex) { "Page" }) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                DialogHeader("Edit Names", onClose = onDismiss)
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = bankName,
                        onValueChange = { bankName = it },
                        label = { Text("Bank Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pageName,
                        onValueChange = { pageName = it },
                        label = { Text("Page Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                DialogFooter(onCancel = onDismiss, onSave = { onSaveBank(bankName); onSavePage(pageName); onDismiss() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSampleDialog(
    sample: SamplePad,
    onDismiss: () -> Unit,
    onSave: (SamplePad) -> Unit,
    onLoadFile: () -> Unit,
    onSelectFromLibrary: () -> Unit,
    onClearAudio: () -> Unit
) {
    var name by remember(sample.name) { mutableStateOf(sample.name) }
    var volume by remember { mutableFloatStateOf(sample.volume.toFloat()) }
    var loop by remember { mutableStateOf(sample.loop) }
    var selectedColor by remember { mutableStateOf(sample.color) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxHeight(0.85f) // Limit height
        ) {
            Column {
                DialogHeader("Edit Sample", onClose = onDismiss)

                Column(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Button Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Audio Source", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onLoadFile, Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Load File") }
                        Button(onClick = onSelectFromLibrary, Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) { Text("Library") }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text("Pad Color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))

                    // Inline Color Picker
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5), // Slightly more dense
                        modifier = Modifier.height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(getModxColors()) { modxColor ->
                            val colorInt = try {
                                android.graphics.Color.parseColor(modxColor.hex)
                            } catch (e: Exception) { android.graphics.Color.GRAY }
                            val isSelected = selectedColor.equals(modxColor.hex, ignoreCase = true)

                            Box(
                                Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorInt))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = modxColor.hex }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Volume: ${volume.toInt()}", modifier = Modifier.width(80.dp))
                        Slider(
                            value = volume,
                            onValueChange = { volume = it },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { loop = !loop }) {
                        Checkbox(checked = loop, onCheckedChange = { loop = it })
                        Text("Loop Playback")
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onClearAudio,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Clear Audio Data") }
                    Spacer(Modifier.height(8.dp))
                }

                DialogFooter(
                    onCancel = onDismiss,
                    onSave = {
                        onSave(sample.copy(name = name, volume = volume.toInt(), loop = loop, color = selectedColor))
                        onDismiss()
                    },
                    saveLabel = "OK"
                )
            }
        }
    }
}

@Composable
fun SwapDialog(
    currentPageSlots: List<PatchSlot>,
    sourceSlot: PatchSlot,
    onDismiss: () -> Unit,
    onSelectSlot: (PatchSlot) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(12.dp)) {
            Column {
                DialogHeader("Swap \"${sourceSlot.getDisplayName()}\" with...", onClose = onDismiss)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(currentPageSlots.filter { it.id != sourceSlot.id }) { slot ->
                        val bgColor = try {
                            Color(android.graphics.Color.parseColor(slot.color))
                        } catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }

                        Card(
                            modifier = Modifier
                                .aspectRatio(1.3f)
                                .clickable { onSelectSlot(slot) },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    slot.getDisplayName(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                DialogFooter(onCancel = onDismiss, onSave = {}, showCancel = true, saveLabel = "") // Only cancel needed
            }
        }
    }
}

// --- MASTER EDIT SLOT DIALOG (REFACTORED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotDialog(
    slot: PatchSlot,
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(slot.name) { mutableStateOf(slot.name) }
    var displayNameType by remember(slot.displayNameType) { mutableStateOf(slot.displayNameType) }
    var assignedSample by remember(slot.assignedSample) { mutableIntStateOf(slot.assignedSample) }
    var slotColorHex by remember(slot.color) { mutableStateOf(slot.color) }
    var volume by remember(slot.volume) { mutableFloatStateOf(slot.volume.toFloat()) }

    var perfCategory by remember { mutableStateOf("For Montage (Single)") }
    var perfBankIndex by remember { mutableIntStateOf(0) }
    var perfSearch by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onEvent(MainEvent.ShowPerformanceBrowser(slot))
        onEvent(MainEvent.SelectPerformanceCategory(perfCategory))
    }
    LaunchedEffect(perfCategory) {
        onEvent(MainEvent.SelectPerformanceCategory(perfCategory))
        perfBankIndex = 0
    }
    LaunchedEffect(perfCategory, perfBankIndex) {
        onEvent(MainEvent.SelectPerformanceBank(perfBankIndex))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Full width for landscape
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f) // Taller to maximize space
        ) {
            Column {
                // 1. Compact Header
                DialogHeader("Edit Slot ${slot.getSlotNumber()}", onClose = onDismiss)

                // 2. Single Scrollable Content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // --- SECTION: BASIC SETTINGS ---
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("General Settings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Custom Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))

                        Text("Display Mode", style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = displayNameType == DisplayNameType.PERFORMANCE,
                                onClick = { displayNameType = DisplayNameType.PERFORMANCE }
                            )
                            Text("Performance Name", fontSize = 14.sp)
                            Spacer(Modifier.width(16.dp))
                            RadioButton(
                                selected = displayNameType == DisplayNameType.CUSTOM,
                                onClick = { displayNameType = DisplayNameType.CUSTOM }
                            )
                            Text("Custom Name", fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(16.dp))

                        // Inline Color Picker (New!)
                        Text("Slot Color", style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp))
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 40.dp),
                            modifier = Modifier.height(100.dp), // Fixed height for colors
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(getModxColors()) { modxColor ->
                                val colorInt = try {
                                    android.graphics.Color.parseColor(modxColor.hex)
                                } catch (e: Exception) { android.graphics.Color.GRAY }
                                val isSelected = slotColorHex.equals(modxColor.hex, ignoreCase = true)

                                Box(
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(colorInt))
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { slotColorHex = modxColor.hex }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))

                        // --- SECTION: MIDI & AUDIO ---
                        Text("MIDI & Audio", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))

                        val displayPerf = uiState.slotToEditColor?.performanceName ?: slot.performanceName
                        OutlinedTextField(
                            value = displayPerf,
                            onValueChange = {},
                            label = { Text("Assigned Performance") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Volume: ${volume.toInt()}", modifier = Modifier.width(80.dp))
                            Slider(
                                value = volume,
                                onValueChange = { volume = it },
                                valueRange = 0f..127f,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        var sampleExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.samples.find { it.id == assignedSample }?.name ?: "None",
                                onValueChange = {},
                                label = { Text("Trigger Sample Pad") },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sampleExpanded) },
                                modifier = Modifier.fillMaxWidth().clickable { sampleExpanded = true }
                            )
                            Box(Modifier.matchParentSize().clickable { sampleExpanded = true })
                            DropdownMenu(
                                expanded = sampleExpanded,
                                onDismissRequest = { sampleExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None") },
                                    onClick = { assignedSample = -1; sampleExpanded = false }
                                )
                                uiState.samples.forEach { s ->
                                    DropdownMenuItem(
                                        text = { Text(s.name) },
                                        onClick = { assignedSample = s.id; sampleExpanded = false }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))

                        // --- SECTION: ACTIONS ---
                        Text("Actions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))

                        // 2x2 Grid for Actions (Color button removed)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onEvent(MainEvent.CopySlot(slot)); onDismiss() },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Copy") }
                                Button(
                                    onClick = { onEvent(MainEvent.ShowPasteConfirmDialog(slot)) },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Paste") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onEvent(MainEvent.ShowSwapDialog(slot)) },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Swap") }
                                Button(
                                    onClick = { onEvent(MainEvent.ShowClearConfirmDialog(slot)) },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                    shape = RoundedCornerShape(8.dp)
                                ) { Text("Clear") }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(16.dp))
                    }

                    // --- SECTION: BROWSER ---
                    item {
                        Text("Performance Browser", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(12.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Category Select
                            var catExpanded by remember { mutableStateOf(false) }
                            Box(Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = perfCategory,
                                    onValueChange = {},
                                    label = { Text("Category") },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                                    modifier = Modifier.fillMaxWidth().clickable { catExpanded = true }
                                )
                                Box(Modifier.matchParentSize().clickable { catExpanded = true })
                                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                    uiState.performanceCategories.forEach { cat ->
                                        DropdownMenuItem(text = { Text(cat) }, onClick = { perfCategory = cat; catExpanded = false })
                                    }
                                }
                            }
                            // Bank Select
                            var bankExpanded by remember { mutableStateOf(false) }
                            Box(Modifier.weight(1f)) {
                                val bankName = uiState.performanceBanks.getOrNull(perfBankIndex)?.name ?: "Select Bank"
                                OutlinedTextField(
                                    value = bankName,
                                    onValueChange = {},
                                    label = { Text("Bank") },
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                                    modifier = Modifier.fillMaxWidth().clickable { bankExpanded = true }
                                )
                                Box(Modifier.matchParentSize().clickable { bankExpanded = true })
                                DropdownMenu(expanded = bankExpanded, onDismissRequest = { bankExpanded = false }) {
                                    uiState.performanceBanks.forEachIndexed { idx, bank ->
                                        DropdownMenuItem(text = { Text(bank.name) }, onClick = { perfBankIndex = idx; bankExpanded = false })
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = perfSearch,
                            onValueChange = { perfSearch = it },
                            placeholder = { Text("Search Performance...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    // Performance List
                    items(uiState.performances.filter { it.name.contains(perfSearch, true) }) { perf ->
                        val isSelected = uiState.slotToEditColor?.performanceName == perf.name
                        Surface(
                            onClick = { onEvent(MainEvent.SelectPerformance(perf)) },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(vertical = 12.dp, horizontal = 8.dp)) {
                                Text(
                                    text = perf.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    }
                }

                // 3. Compact Footer
                DialogFooter(
                    onCancel = onDismiss,
                    onSave = {
                        val finalSlot = slot.copy(
                            name = name,
                            displayNameType = displayNameType,
                            assignedSample = assignedSample,
                            color = slotColorHex,
                            volume = volume.toInt(),
                            performanceName = uiState.slotToEditColor?.performanceName ?: slot.performanceName,
                            msb = uiState.slotToEditColor?.msb ?: slot.msb,
                            lsb = uiState.slotToEditColor?.lsb ?: slot.lsb,
                            pc = uiState.slotToEditColor?.pc ?: slot.pc
                        )
                        onEvent(MainEvent.UpdateSlot(finalSlot))
                        onDismiss()
                    },
                    saveLabel = "Save Changes"
                )
            }
        }
    }
}

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
    val filteredList = remember(library, searchQuery) {
        library.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxHeight(0.8f)
        ) {
            Column {
                DialogHeader("Audio Library", onClose = onDismiss)
                Column(Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by name...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        Modifier
                            .weight(1f)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                    ) {
                        items(filteredList) { item ->
                            Surface(
                                onClick = { selectedItem = item },
                                color = if (selectedItem == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(item.name, modifier = Modifier.padding(12.dp))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = onAddFile) { Text("Add File") }
                        Button(
                            onClick = { selectedItem?.let { onDelete(it); selectedItem = null } },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            enabled = selectedItem != null
                        ) { Text("Delete") }
                    }
                }
                DialogFooter(
                    onCancel = onDismiss,
                    onSave = { selectedItem?.let { onSelect(it); onDismiss() } },
                    saveLabel = "Select"
                )
            }
        }
    }
}

@Composable
fun HandleDialogs(
    uiState: MainUiState.Success,
    viewModel: MainViewModel,
    libraryPickerLauncher: ActivityResultLauncher<String>
) {
    if (uiState.showResetDialog) ConfirmationDialog(
        "Reset All",
        "Are you sure you want to reset all data? This cannot be undone.",
        { viewModel.onEvent(MainEvent.ResetData) },
        { viewModel.onEvent(MainEvent.ShowResetDialog(false)) })

    if (uiState.showBankPageNameDialog) BankPageNameDialog(
        uiState,
        { viewModel.onEvent(MainEvent.ShowBankPageNameDialog(false)) },
        { viewModel.onEvent(MainEvent.UpdateBankName(uiState.settings.currentBankIndex, it)) },
        { viewModel.onEvent(MainEvent.UpdatePageName(uiState.settings.currentPageIndex, it)) })

    uiState.editingSample?.let { sample ->
        EditSampleDialog(
            sample,
            { viewModel.onEvent(MainEvent.ShowEditSampleDialog(null)) },
            { viewModel.onEvent(MainEvent.UpdateSample(it)) },
            { viewModel.onEvent(MainEvent.LoadSampleFile) },
            { viewModel.onEvent(MainEvent.ShowAudioLibrary(true, sample.id)) },
            { viewModel.onEvent(MainEvent.ClearSampleAudio(sample.id)) }
        )
    }

    if (uiState.showAudioLibrary) AudioLibraryDialog(
        uiState.audioLibrary,
        { viewModel.onEvent(MainEvent.ShowAudioLibrary(false)) },
        { viewModel.onEvent(MainEvent.SelectSampleFromLibrary(it)) },
        { viewModel.onEvent(MainEvent.DeleteFromAudioLibrary(it)) },
        { libraryPickerLauncher.launch("audio/*") })

    uiState.slotToPaste?.let { slot ->
        ConfirmationDialog(
            "Paste",
            "Paste clipboard data over '${slot.getDisplayName()}'?",
            { viewModel.onEvent(MainEvent.PasteSlot(slot)) },
            { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(null)) })
    }

    uiState.slotToClear?.let { slot ->
        ConfirmationDialog(
            "Clear Slot",
            "Are you sure you want to clear slot '${slot.getDisplayName()}'?",
            { viewModel.onEvent(MainEvent.ClearSlot(slot)) },
            { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(null)) })
    }

    uiState.slotToSwap?.let { slot ->
        val currentPageSlots = uiState.patchData.banks.getOrNull(uiState.settings.currentBankIndex)?.pages?.getOrNull(uiState.settings.currentPageIndex)?.slots ?: emptyList()
        SwapDialog(
            currentPageSlots,
            slot,
            { viewModel.onEvent(MainEvent.ShowSwapDialog(null)) },
            { target: PatchSlot -> viewModel.onEvent(MainEvent.SwapSlots(slot.id, target.id)) })
    }

    uiState.slotToEditColor?.let { slot ->
        EditSlotDialog(
            slot,
            uiState,
            viewModel::onEvent,
            { viewModel.onEvent(MainEvent.ShowSlotColorDialog(null)) })
    }
}