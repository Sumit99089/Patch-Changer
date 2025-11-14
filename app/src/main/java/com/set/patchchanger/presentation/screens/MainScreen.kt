package com.set.patchchanger.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
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
    val currentTheme = (uiState as? MainUiState.Success)?.settings?.theme ?: AppTheme.BLACK

    // We need to wrap the content in our custom theme that observes the state
    // NOTE: The theme selection logic is not fully implemented in the original Theme.kt
    // For now, we use the default dark/light theme.
    PatchChangerTheme {
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

    // Audio file picker
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = viewModel.getFileName(it)
            viewModel.onEvent(MainEvent.SetSampleFile(it, name))
        }
    }

    // Audio file picker for library
    val libraryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val name = viewModel.getFileName(it)
            viewModel.onEvent(MainEvent.AddFileToLibrary(it, name))
        }
    }

    // Handle Events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.RequestFilePicker -> audioPickerLauncher.launch("audio/*")
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
                onThemeClick = { /* TODO: Show Theme Dialog */ }
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
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        SelectorBar(state, viewModel::onEvent)
                        Spacer(Modifier.height(8.dp))
                        PatchGrid(
                            patchData = state.patchData,
                            currentBankIndex = state.settings.currentBankIndex,
                            currentPageIndex = state.settings.currentPageIndex,
                            isEditMode = isEditMode,
                            onSlotClick = { slot ->
                                if (isEditMode) {
                                    viewModel.onEvent(MainEvent.ShowSlotColorDialog(slot))
                                } else {
                                    viewModel.onEvent(MainEvent.SelectSlot(slot.id))
                                }
                            },
                            onSlotLongClick = { slot ->
                                viewModel.onEvent(MainEvent.ShowSlotColorDialog(slot))
                            }
                        )
                    }

                    // --- Dialogs ---
                    if (state.showResetDialog) {
                        ConfirmationDialog(
                            title = "Reset All Data",
                            text = "Are you sure you want to reset all data, including the audio library?",
                            onConfirm = { viewModel.onEvent(MainEvent.ResetData) },
                            onDismiss = { viewModel.onEvent(MainEvent.ShowResetDialog(false)) }
                        )
                    }

                    if (state.showBankPageNameDialog) {
                        BankPageNameDialog(
                            state = state,
                            onDismiss = { viewModel.onEvent(MainEvent.ShowBankPageNameDialog(false)) },
                            onSaveBank = {
                                viewModel.onEvent(
                                    MainEvent.UpdateBankName(
                                        state.settings.currentBankIndex,
                                        it
                                    )
                                )
                            },
                            onSavePage = {
                                viewModel.onEvent(
                                    MainEvent.UpdatePageName(
                                        state.settings.currentPageIndex,
                                        it
                                    )
                                )
                            }
                        )
                    }

                    state.editingSample?.let { sample ->
                        EditSampleDialog(
                            sample = sample,
                            onDismiss = { viewModel.onEvent(MainEvent.ShowEditSampleDialog(null)) },
                            onSave = { viewModel.onEvent(MainEvent.UpdateSample(it)) },
                            onLoadFile = { viewModel.onEvent(MainEvent.LoadSampleFile) },
                            onSelectFromLibrary = {
                                viewModel.onEvent(
                                    MainEvent.ShowAudioLibrary(
                                        true,
                                        sample.id
                                    )
                                )
                            },
                            onClearAudio = { viewModel.onEvent(MainEvent.ClearSampleAudio(sample.id)) },
                            onEditColor = { viewModel.onEvent(MainEvent.ShowSampleColorDialog(sample)) }
                        )
                    }

                    if (state.showAudioLibrary) {
                        AudioLibraryDialog(
                            library = state.audioLibrary,
                            onDismiss = { viewModel.onEvent(MainEvent.ShowAudioLibrary(false)) },
                            onSelect = { viewModel.onEvent(MainEvent.SelectSampleFromLibrary(it)) },
                            onDelete = { viewModel.onEvent(MainEvent.DeleteFromAudioLibrary(it)) },
                            onAddFile = { libraryPickerLauncher.launch("audio/*") }
                        )
                    }

                    state.slotToPaste?.let { slot ->
                        ConfirmationDialog(
                            title = "Confirm Paste",
                            text = "Paste '${viewModel.internalState.value.slotToPaste?.getDisplayName() ?: "..."}' over '${slot.getDisplayName()}'?",
                            onConfirm = { viewModel.onEvent(MainEvent.PasteSlot(slot)) },
                            onDismiss = { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(null)) }
                        )
                    }

                    state.slotToClear?.let { slot ->
                        ConfirmationDialog(
                            title = "Clear Slot",
                            text = "Are you sure you want to clear slot '${slot.getDisplayName()}'?",
                            onConfirm = { viewModel.onEvent(MainEvent.ClearSlot(slot)) },
                            onDismiss = { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(null)) }
                        )
                    }

                    state.slotToSwap?.let { slot ->
                        SwapDialog(
                            currentPageSlots = state.patchData.banks[state.settings.currentBankIndex].pages[state.settings.currentPageIndex].slots,
                            sourceSlot = slot,
                            onDismiss = { viewModel.onEvent(MainEvent.ShowSwapDialog(null)) },
                            onSelectSlot = { targetSlot ->
                                viewModel.onEvent(MainEvent.SwapSlots(slot.id, targetSlot.id))
                            }
                        )
                    }

                    state.slotToEditColor?.let { slot ->
                        EditSlotDialog(
                            slot = slot,
                            onDismiss = { viewModel.onEvent(MainEvent.ShowSlotColorDialog(null)) },
                            onSave = { viewModel.onEvent(MainEvent.UpdateSlot(it)) },
                            onCopy = { viewModel.onEvent(MainEvent.CopySlot(slot)) },
                            onPaste = { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(slot)) },
                            onSwap = { viewModel.onEvent(MainEvent.ShowSwapDialog(slot)) },
                            onClear = { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(slot)) },
                            samples = state.samples
                        )
                    }

                    state.sampleToEditColor?.let { sample ->
                        ColorPickerDialog(
                            onDismiss = { viewModel.onEvent(MainEvent.ShowSampleColorDialog(null)) },
                            onColorSelected = { colorHex ->
                                viewModel.onEvent(MainEvent.UpdateSample(sample.copy(color = colorHex)))
                                viewModel.onEvent(MainEvent.ShowSampleColorDialog(null))
                            }
                        )
                    }

                }

                is MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainUiState.Error -> Text(
                    "Error: ${state.message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(uiState: MainUiState, onEvent: (MainEvent) -> Unit, isEditMode: Boolean) {
    val settings = (uiState as? MainUiState.Success)?.settings
    val midiState = (uiState as? MainUiState.Success)?.midiState

    // Background color animates based on connection state
    val barColor =
        if (midiState is MidiConnectionState.Connected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant

    TopAppBar(
        title = {
            Column {
                Text("Set Patch Chang", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (isEditMode) "EDIT MODE" else "SRIKANTA",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEditMode) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.7f
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor),
        actions = {
            // Transpose
            settings?.let { s ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(-1)) }) {
                        Text(
                            "-",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (s.currentTranspose > 0) "+${s.currentTranspose}" else "${s.currentTranspose}",
                        color = if (s.currentTranspose != 0) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onEvent(MainEvent.ResetTranspose) }
                    )
                    IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(1)) }) {
                        Text(
                            "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // MIDI Status Icon
            IconButton(onClick = {
                if (midiState is MidiConnectionState.Connected) onEvent(MainEvent.DisconnectMidi) else onEvent(
                    MainEvent.ConnectMidi
                )
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
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Selector(
            label = "Page",
            value = state.patchData.pageNames.getOrElse(state.settings.currentPageIndex) { "" },
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun Selector(
    label: String,
    value: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrev) {
            Icon(
                Icons.Default.ArrowUpward,
                null
            )
        } // Up is Prev
        Card(
            Modifier
                .weight(1f)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onNext) { Icon(Icons.Default.ArrowDownward, null) } // Down is Next
    }
}

@Composable
fun PatchGrid(
    patchData: PatchData,
    currentBankIndex: Int,
    currentPageIndex: Int,
    isEditMode: Boolean,
    onSlotClick: (PatchSlot) -> Unit,
    onSlotLongClick: (PatchSlot) -> Unit
) {
    val page = patchData.banks.getOrNull(currentBankIndex)?.pages?.getOrNull(currentPageIndex)

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f) // Leave space for bottom bar
    ) {
        page?.slots?.let { slots ->
            items(slots) { slot ->
                val bgColor = try {
                    Color(android.graphics.Color.parseColor(slot.color))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.surfaceVariant
                }
                val borderColor =
                    if (slot.selected && !isEditMode) Color(0xFFFFA726) else if (isEditMode) Color.White.copy(
                        alpha = 0.3f
                    ) else Color.Transparent
                val borderWidth = if (slot.selected || isEditMode) 2.dp else 0.dp

                Card(
                    modifier = Modifier
                        .aspectRatio(1.618f) // Golden ratio for a wider button
                        .clickable {
                            if (isEditMode) {
                                onSlotLongClick(slot) // Open edit dialog on simple tap in edit mode
                            } else {
                                onSlotClick(slot)
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = BorderStroke(borderWidth, borderColor),
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
                            color = Color.White, // Always white text on colored tiles
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

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Sample Pads
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            samples.take(4).forEach { sample ->
                val color = try {
                    Color(android.graphics.Color.parseColor(sample.color))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                Button(
                    onClick = {
                        if (isEditMode) {
                            onEvent(MainEvent.ShowEditSampleDialog(sample))
                        } else {
                            onEvent(MainEvent.TriggerSample(sample.id))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        sample.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Bottom Controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* Save Data Logic */ }) { Icon(Icons.Default.Save, "Save") }
                IconButton(onClick = { /* Load Data Logic */ }) {
                    Icon(
                        Icons.Default.FolderOpen,
                        "Load"
                    )
                }
                TextButton(onClick = { onEvent(MainEvent.ShowResetDialog(true)) }) {
                    Text("X Reset", color = Color.Red)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
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