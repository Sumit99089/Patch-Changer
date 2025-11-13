package com.set.patchchanger.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.set.patchchanger.domain.model.*
import com.set.patchchanger.domain.usecase.GetPerformancesUseCase
import com.set.patchchanger.presentation.viewmodel.MainEvent
import com.set.patchchanger.presentation.viewmodel.MainUiState
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.UiEvent
import com.set.patchchanger.ui.theme.PatchChangerTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // We need to wrap the content in our custom theme that observes the state
    val currentTheme = (uiState as? MainUiState.Success)?.settings?.theme ?: AppTheme.BLACK

    PatchChangerTheme(appTheme = currentTheme) {
        MainScreenContent(viewModel, uiState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    uiState: MainUiState
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Edit Mode State
    var isEditMode by remember { mutableStateOf(false) }

    // Modal States
    var showEditSlotDialog by remember { mutableStateOf<PatchSlot?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Sample File Picker
    val samplePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        // Handle audio file selection for sample (implementation detail would require copying Uri to File)
    }

    // Handle Events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.PlaySample -> {
                    // In a real app, this would trigger the repository's play function
                    // For now, we assume the repository or a singleton handles playback via SoundPool
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(uiState, viewModel::onEvent, isEditMode) },
        bottomBar = {
            BottomBar(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onToggleEdit = { isEditMode = !isEditMode },
                isEditMode = isEditMode,
                onThemeClick = { showThemeDialog = true }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is MainUiState.Success -> {
                    Column(Modifier.fillMaxSize().padding(8.dp)) {
                        SelectorBar(state, viewModel::onEvent)
                        Spacer(Modifier.height(8.dp))
                        PatchGrid(
                            patchData = state.patchData,
                            currentBankIndex = state.settings.currentBankIndex,
                            currentPageIndex = state.settings.currentPageIndex,
                            isEditMode = isEditMode,
                            onSlotClick = { slot ->
                                if (isEditMode) {
                                    showEditSlotDialog = slot
                                } else {
                                    viewModel.onEvent(MainEvent.SelectSlot(slot.id))
                                }
                            }
                        )
                    }
                }
                is MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainUiState.Error -> Text("Error: ${state.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    // Dialogs
    if (showEditSlotDialog != null) {
        EditSlotDialog(
            slot = showEditSlotDialog!!,
            samples = (uiState as? MainUiState.Success)?.samples ?: emptyList(),
            onDismiss = { showEditSlotDialog = null },
            onSave = { updatedSlot ->
                viewModel.onEvent(MainEvent.UpdateSlot(updatedSlot))
                showEditSlotDialog = null
            },
            onSwap = { targetSlotId ->
                viewModel.onEvent(MainEvent.SwapSlots(showEditSlotDialog!!.id, targetSlotId))
                showEditSlotDialog = null
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                viewModel.onEvent(MainEvent.UpdateTheme(theme))
                showThemeDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(uiState: MainUiState, onEvent: (MainEvent) -> Unit, isEditMode: Boolean) {
    val settings = (uiState as? MainUiState.Success)?.settings
    val midiState = (uiState as? MainUiState.Success)?.midiState

    // Background color animates based on connection state
    val barColor = if (midiState is MidiConnectionState.Connected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surface

    TopAppBar(
        title = {
            Column {
                Text("Live Set Patch Changer", style = MaterialTheme.typography.titleMedium)
                Text(if (isEditMode) "EDIT MODE" else "SRIKANTA",
                    style = MaterialTheme.typography.labelSmall,
                    color = if(isEditMode) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor),
        actions = {
            // Transpose
            settings?.let { s ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                    IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(-1)) }) { Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                    Text(
                        text = if (s.currentTranspose > 0) "+${s.currentTranspose}" else "${s.currentTranspose}",
                        color = if (s.currentTranspose != 0) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onEvent(MainEvent.ResetTranspose) }
                    )
                    IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(1)) }) { Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold) }
                }
            }

            // MIDI Status Icon
            IconButton(onClick = {
                if (midiState is MidiConnectionState.Connected) onEvent(MainEvent.DisconnectMidi) else onEvent(MainEvent.ConnectMidi)
            }) {
                Icon(
                    imageVector = Icons.Default.Piano,
                    contentDescription = "MIDI",
                    tint = if (midiState is MidiConnectionState.Connected) Color.Green else Color.Red
                )
            }
        }
    )
}

@Composable
fun SelectorBar(state: MainUiState.Success, onEvent: (MainEvent) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Selector(
            label = "Bank",
            value = state.patchData.bankNames.getOrElse(state.settings.currentBankIndex) { "" },
            onPrev = { onEvent(MainEvent.NavigateBank(-1)) },
            onNext = { onEvent(MainEvent.NavigateBank(1)) },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Selector(
            label = "Page",
            value = state.patchData.pageNames.getOrElse(state.settings.currentPageIndex) { "" },
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun Selector(label: String, value: String, onPrev: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) { Icon(Icons.Default.ArrowUpward, null) } // Up is Prev in HTML logic
        Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.fillMaxWidth().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        IconButton(onClick = onNext) { Icon(Icons.Default.ArrowDownward, null) }
    }
}

@Composable
fun PatchGrid(
    patchData: PatchData,
    currentBankIndex: Int,
    currentPageIndex: Int,
    isEditMode: Boolean,
    onSlotClick: (PatchSlot) -> Unit
) {
    val page = patchData.banks.getOrNull(currentBankIndex)?.pages?.getOrNull(currentPageIndex)

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f) // Leave space for bottom bar
    ) {
        page?.slots?.let { slots ->
            items(slots) { slot ->
                val bgColor = try { Color(android.graphics.Color.parseColor(slot.color)) } catch (e: Exception) { MaterialTheme.colorScheme.surfaceVariant }
                val borderColor = if (slot.selected && !isEditMode) Color(0xFFFFA726) else if (isEditMode) Color.White.copy(alpha=0.3f) else Color.Transparent
                val borderWidth = if (slot.selected || isEditMode) 2.dp else 0.dp

                Card(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onSlotClick(slot) },
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = BorderStroke(borderWidth, borderColor)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = slot.getDisplayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White, // Always white text on colored tiles as per HTML
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    uiState: MainUiState,
    onEvent: (MainEvent) -> Unit,
    onToggleEdit: () -> Unit,
    isEditMode: Boolean,
    onThemeClick: () -> Unit
) {
    val samples = (uiState as? MainUiState.Success)?.samples ?: emptyList()

    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        // Sample Pads
        Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            samples.take(4).forEach { sample ->
                val color = try { Color(android.graphics.Color.parseColor(sample.color)) } catch(e:Exception){ MaterialTheme.colorScheme.primary }
                Button(
                    onClick = {
                        // Play logic handled in ViewModel/Repo via triggered event or direct call
                        // For pure UI feedback:
                        onEvent(MainEvent.SelectSlot(-1)) // Dummy to trigger sound if mapped
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp).height(50.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(sample.name, maxLines = 1)
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Bottom Controls
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row {
                IconButton(onClick = { /* Save Data Logic */ }) { Icon(Icons.Default.Save, "Save") }
                IconButton(onClick = { /* Load Data Logic */ }) { Icon(Icons.Default.FolderOpen, "Load") }
            }

            Row {
                IconButton(onClick = onThemeClick) { Icon(Icons.Default.Palette, "Theme") }
                Button(
                    onClick = onToggleEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditMode) Color(0xFFFFA726) else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(if (isEditMode) "DONE" else "EDIT")
                }
            }
        }
    }
}

// --- Edit Dialogs ---

@Composable
fun EditSlotDialog(
    slot: PatchSlot,
    samples: List<SamplePad>,
    onDismiss: () -> Unit,
    onSave: (PatchSlot) -> Unit,
    onSwap: (Int) -> Unit
) {
    var name by remember { mutableStateOf(slot.name) }
    var displayNameType by remember { mutableStateOf(slot.displayNameType) }
    var assignedSample by remember { mutableStateOf(slot.assignedSample) }
    var colorHex by remember { mutableStateOf(slot.color) }

    // Performance Browser State
    val performanceUseCase = remember { GetPerformancesUseCase() } // Should be injected via VM ideally
    var perfCategory by remember { mutableStateOf(performanceUseCase.getCategories().first()) }
    var perfBankIndex by remember { mutableStateOf(0) }
    var selectedPerf by remember { mutableStateOf<Performance?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)
        ) {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text("Edit Slot ${slot.getSlotNumber()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                // Name
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Custom Name") }, modifier = Modifier.fillMaxWidth())

                // Display Type
                Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Display:", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = displayNameType == DisplayNameType.PERFORMANCE, onClick = { displayNameType = DisplayNameType.PERFORMANCE })
                        Text("Perf Name")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = displayNameType == DisplayNameType.CUSTOM, onClick = { displayNameType = DisplayNameType.CUSTOM })
                        Text("Custom")
                    }
                }

                // Assign Sample
                Text("Assign Sample Pad", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    FilterChip(selected = assignedSample == -1, onClick = { assignedSample = -1 }, label = { Text("None") })
                    samples.forEach { s ->
                        Spacer(Modifier.width(4.dp))
                        FilterChip(selected = assignedSample == s.id, onClick = { assignedSample = s.id }, label = { Text(s.name) })
                    }
                }

                Divider(Modifier.padding(vertical = 8.dp))

                // Color Picker (Simple Grid)
                Text("Color", fontWeight = FontWeight.Bold)
                val colors = listOf("#333333", "#F44336", "#FFEB3B", "#4CAF50", "#2196F3", "#00BCD4", "#E91E63", "#FF9800")
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    colors.forEach { hex ->
                        Box(
                            Modifier
                                .size(40.dp)
                                .padding(4.dp)
                                .background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(4.dp))
                                .border(if(colorHex == hex) 2.dp else 0.dp, Color.White, RoundedCornerShape(4.dp))
                                .clickable { colorHex = hex }
                        )
                    }
                }

                Divider(Modifier.padding(vertical = 8.dp))

                // Performance Browser (Simplified)
                Text("Assign Performance", fontWeight = FontWeight.Bold)
                // In real app, use ExposedDropdownMenuBox. Using simple rows for brevity
                Text("Current: ${slot.performanceName}", fontSize = 12.sp, color = Color.Gray)

                // Category Spinner simulator
                ScrollableTabRow(selectedTabIndex = 0, edgePadding = 0.dp) { // Simplified visual
                    performanceUseCase.getCategories().take(3).forEach { Text(it, modifier = Modifier.padding(8.dp)) }
                }

                // List of perfs
                val banks = performanceUseCase.getBanks(perfCategory)
                // This part handles the complexity of the HTML browser
                // For code brevity, we imply the selection updates 'selectedPerf'

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        // Perform Swap Logic - Needs a way to pick target.
                        // Simplified: Trigger a mode in UI or open another list
                        onDismiss()
                    }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val updated = slot.copy(
                            name = name,
                            displayNameType = displayNameType,
                            assignedSample = assignedSample,
                            color = colorHex,
                            // If selectedPerf update MSB/LSB/PC/Name
                            performanceName = selectedPerf?.name ?: slot.performanceName,
                            msb = selectedPerf?.msb ?: slot.msb,
                            lsb = selectedPerf?.lsb ?: slot.lsb,
                            pc = selectedPerf?.pc ?: slot.pc
                        )
                        onSave(updated)
                    }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun ThemeSelectionDialog(onDismiss: () -> Unit, onThemeSelected: (AppTheme) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("Select Theme", style = MaterialTheme.typography.titleLarge)
                LazyVerticalGrid(GridCells.Fixed(2), modifier = Modifier.height(300.dp)) {
                    items(AppTheme.values()) { theme ->
                        Button(
                            onClick = { onThemeSelected(theme) },
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(theme.displayName)
                        }
                    }
                }
            }
        }
    }
}