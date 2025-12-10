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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.set.patchchanger.domain.model.AudioLibraryItem
import com.set.patchchanger.domain.model.DisplayNameType
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.getModxColors

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
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
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Edit Names",
                    style = MaterialTheme.typography.titleLarge
                ); Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") }); Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pageName,
                    onValueChange = { pageName = it },
                    label = { Text("Page Name") }); Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = { onSaveBank(bankName); onSavePage(pageName); onDismiss() }) {
                        Text(
                            "Save"
                        )
                    }
                }
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
    onClearAudio: () -> Unit,
    onEditColor: () -> Unit
) {
    // MODIFIED: Added sample.name key to remember to update UI when file loads
    var name by remember(sample.name) { mutableStateOf(sample.name) }

    var volume by remember { mutableFloatStateOf(sample.volume.toFloat()) }
    var loop by remember { mutableStateOf(sample.loop) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Edit Sample",
                    style = MaterialTheme.typography.titleLarge
                ); Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }); Spacer(Modifier.height(8.dp))
                Row {
                    Button(onClick = onLoadFile, Modifier.weight(1f)) { Text("File") }; Spacer(
                    Modifier.width(8.dp)
                )
                    Button(onClick = onSelectFromLibrary, Modifier.weight(1f)) { Text("Lib") }
                }
                Spacer(Modifier.height(8.dp)); Button(
                onClick = onEditColor,
                Modifier.fillMaxWidth()
            ) { Text("Color") }
                Spacer(Modifier.height(8.dp)); Text("Vol: ${volume.toInt()}"); Slider(
                value = volume,
                onValueChange = { volume = it },
                valueRange = 0f..100f
            )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = loop,
                        onCheckedChange = { loop = it }); Text("Loop")
                }
                Spacer(Modifier.height(16.dp)); Button(
                onClick = onClearAudio,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Clear") }
                Spacer(Modifier.height(16.dp)); Button(onClick = {
                onSave(
                    sample.copy(
                        name = name,
                        volume = volume.toInt(),
                        loop = loop
                    )
                ); onDismiss()
            }) { Text("OK") }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(onDismiss: () -> Unit, onColorSelected: (String) -> Unit) {
    val colors = getModxColors()
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Select Color",
                    style = MaterialTheme.typography.titleLarge
                ); Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(colors) { c ->
                        Box(
                            Modifier
                                .padding(4.dp)
                                .aspectRatio(1f)
                                .background(Color(c.hex.toColorInt()), CircleShape)
                                .clickable { onColorSelected(c.hex) })
                    }
                }
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
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Swap \"${sourceSlot.getDisplayName()}\" with...",
                    style = MaterialTheme.typography.titleLarge
                ); Spacer(Modifier.height(16.dp))
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
                            Color(slot.color.toColorInt())
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier
                                .aspectRatio(1.5f)
                                .clickable { onSelectSlot(slot) },
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    slot.getDisplayName(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp)); TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Cancel") }
            }
        }
    }
}

// --- MASTER EDIT SLOT DIALOG ---
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
    var showColorPicker by remember { mutableStateOf(false) }

    // HTML-feature: Slot Volume
    var volume by remember(slot.volume) { mutableFloatStateOf(slot.volume.toFloat()) }

    var perfCategory by remember { mutableStateOf("For Montage (Single)") }
    var perfBankIndex by remember { mutableIntStateOf(0) }
    var perfSearch by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onEvent(MainEvent.ShowPerformanceBrowser(slot)); onEvent(
        MainEvent.SelectPerformanceCategory(
            perfCategory
        )
    )
    }
    LaunchedEffect(perfCategory) {
        onEvent(MainEvent.SelectPerformanceCategory(perfCategory)); perfBankIndex = 0
    }
    LaunchedEffect(perfCategory, perfBankIndex) {
        onEvent(MainEvent.SelectPerformanceBank(perfBankIndex))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f)
                    .padding(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Edit Slot: ${slot.getSlotNumber()}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = onDismiss) { Text("✕") }
                    }
                    HorizontalDivider()

                    Row(
                        Modifier
                            .weight(1f)
                            .padding(top = 16.dp)
                    ) {
                        // LEFT: Settings
                        Column(
                            Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(end = 16.dp)
                        ) {
                            Text(
                                "Name",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))

                            Text("Display Mode", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = displayNameType == DisplayNameType.PERFORMANCE,
                                    onClick = {
                                        displayNameType = DisplayNameType.PERFORMANCE
                                    }); Text("Performance", fontSize = 13.sp)
                                Spacer(Modifier.width(8.dp))
                                RadioButton(
                                    selected = displayNameType == DisplayNameType.CUSTOM,
                                    onClick = {
                                        displayNameType = DisplayNameType.CUSTOM
                                    }); Text("Custom", fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(16.dp))

                            Text("Performance", fontWeight = FontWeight.Bold)
                            val displayPerf =
                                uiState.slotToEditColor?.performanceName ?: slot.performanceName
                            OutlinedTextField(
                                value = displayPerf,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))

                            // Slot Volume Control (Added per request)
                            Text("Slot Volume: ${volume.toInt()}", fontWeight = FontWeight.Bold)
                            Slider(
                                value = volume,
                                onValueChange = { volume = it },
                                valueRange = 0f..127f // MIDI standard 0-127
                            )
                            Spacer(Modifier.height(16.dp))

                            Text("Assign Sample Pad", fontWeight = FontWeight.Bold)
                            var sampleExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = uiState.samples.find { it.id == assignedSample }?.name
                                        ?: "None",
                                    onValueChange = {}, readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(
                                            expanded = sampleExpanded
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { sampleExpanded = true }
                                )
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .clickable { sampleExpanded = true })
                                DropdownMenu(
                                    expanded = sampleExpanded,
                                    onDismissRequest = { sampleExpanded = false }) {
                                    DropdownMenuItem(
                                        text = { Text("None") },
                                        onClick = { assignedSample = -1; sampleExpanded = false })
                                    uiState.samples.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s.name) },
                                            onClick = {
                                                assignedSample = s.id; sampleExpanded = false
                                            })
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))

                            Text("Actions", fontWeight = FontWeight.Bold)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showColorPicker = true },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = try {
                                            Color(slotColorHex.toColorInt())
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.primary
                                        }
                                    )
                                ) { Text("Color") }
                                Button(
                                    onClick = { onEvent(MainEvent.CopySlot(slot)); onDismiss() },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) { Text("Copy") }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onEvent(MainEvent.ShowPasteConfirmDialog(slot)) },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) { Text("Paste") }
                                Button(
                                    onClick = { onEvent(MainEvent.ShowSwapDialog(slot)) },
                                    Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) { Text("Swap") }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onEvent(MainEvent.ShowClearConfirmDialog(slot)) },
                                Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Clear") }
                        }

                        // RIGHT: Browser
                        Column(
                            Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                "Browser",
                                fontWeight = FontWeight.Bold
                            ); Spacer(Modifier.height(8.dp))
                            var catExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedTextField(
                                    value = perfCategory,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { catExpanded = true })
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .clickable { catExpanded = true })
                                DropdownMenu(
                                    expanded = catExpanded,
                                    onDismissRequest = { catExpanded = false }) {
                                    uiState.performanceCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat) },
                                            onClick = { perfCategory = cat; catExpanded = false })
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            var bankExpanded by remember { mutableStateOf(false) }
                            Box {
                                val bankName =
                                    uiState.performanceBanks.getOrNull(perfBankIndex)?.name
                                        ?: "Select Bank"
                                OutlinedTextField(
                                    value = bankName,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { bankExpanded = true })
                                Box(
                                    Modifier
                                        .matchParentSize()
                                        .clickable { bankExpanded = true })
                                DropdownMenu(
                                    expanded = bankExpanded,
                                    onDismissRequest = { bankExpanded = false }) {
                                    uiState.performanceBanks.forEachIndexed { idx, bank ->
                                        DropdownMenuItem(
                                            text = { Text(bank.name) },
                                            onClick = { perfBankIndex = idx; bankExpanded = false })
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = perfSearch,
                                onValueChange = { perfSearch = it },
                                placeholder = { Text("Search...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyColumn(
                                Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    )
                            ) {
                                items(uiState.performances.filter {
                                    it.name.contains(
                                        perfSearch,
                                        true
                                    )
                                }) { perf ->
                                    val isSelected =
                                        uiState.slotToEditColor?.performanceName == perf.name
                                    Text(
                                        text = perf.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onEvent(MainEvent.SelectPerformance(perf)) }
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .padding(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val finalSlot = slot.copy(
                                name = name,
                                displayNameType = displayNameType,
                                assignedSample = assignedSample,
                                color = slotColorHex,
                                volume = volume.toInt(), // Save volume
                                performanceName = uiState.slotToEditColor?.performanceName
                                    ?: slot.performanceName,
                                msb = uiState.slotToEditColor?.msb ?: slot.msb,
                                lsb = uiState.slotToEditColor?.lsb ?: slot.lsb,
                                pc = uiState.slotToEditColor?.pc ?: slot.pc
                            )
                            onEvent(MainEvent.UpdateSlot(finalSlot)); onDismiss()
                        }) { Text("Save Changes") }
                    }
                }
            }
            if (showColorPicker) ColorPickerDialog(
                onDismiss = { showColorPicker = false },
                onColorSelected = { hex -> slotColorHex = hex; showColorPicker = false })
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
        library.filter {
            it.name.contains(
                searchQuery,
                ignoreCase = true
            )
        }
    }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxHeight(0.8f)) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Select Audio",
                    style = MaterialTheme.typography.titleLarge
                ); Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                ); Spacer(Modifier.height(8.dp))
                LazyColumn(
                    Modifier
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    items(filteredList) { item ->
                        Text(
                            item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item }
                                .background(if (selectedItem == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = onAddFile) { Text("Add File") }; Button(
                    onClick = {
                        selectedItem?.let {
                            onDelete(
                                it
                            ); selectedItem = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }; Button(onClick = {
                    selectedItem?.let {
                        onSelect(
                            it
                        ); onDismiss()
                    }
                }) { Text("OK") }
                }
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
        "Reset all data?",
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
            { viewModel.onEvent(MainEvent.ClearSampleAudio(sample.id)) },
            { viewModel.onEvent(MainEvent.ShowSampleColorDialog(sample)) })
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
            "Paste over '${slot.getDisplayName()}'?",
            { viewModel.onEvent(MainEvent.PasteSlot(slot)) },
            { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(null)) })
    }
    uiState.slotToClear?.let { slot ->
        ConfirmationDialog(
            "Clear",
            "Clear slot '${slot.getDisplayName()}'?",
            { viewModel.onEvent(MainEvent.ClearSlot(slot)) },
            { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(null)) })
    }
    uiState.slotToSwap?.let { slot ->
        val currentPageSlots =
            uiState.patchData.banks.getOrNull(uiState.settings.currentBankIndex)?.pages?.getOrNull(
                uiState.settings.currentPageIndex
            )?.slots ?: emptyList()
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
    uiState.sampleToEditColor?.let { sample ->
        ColorPickerDialog(
            {
                viewModel.onEvent(
                    MainEvent.ShowSampleColorDialog(
                        null
                    )
                )
            },
            { color ->
                viewModel.onEvent(MainEvent.UpdateSample(sample.copy(color = color))); viewModel.onEvent(
                MainEvent.ShowSampleColorDialog(null)
            )
            }
        )
    }
}