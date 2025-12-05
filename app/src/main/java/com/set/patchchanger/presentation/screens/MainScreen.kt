package com.set.patchchanger.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.presentation.common.components.ConfirmationDialog
import com.set.patchchanger.presentation.main.components.BottomBar
import com.set.patchchanger.presentation.main.components.CompactControlsBar
import com.set.patchchanger.presentation.main.components.CompactSelectorBar
import com.set.patchchanger.presentation.main.components.PatchGrid
import com.set.patchchanger.presentation.main.components.TopBar
import com.set.patchchanger.presentation.main.dialogs.AudioLibraryDialog
import com.set.patchchanger.presentation.main.dialogs.BankPageNameDialog
import com.set.patchchanger.presentation.main.dialogs.ColorPickerDialog
import com.set.patchchanger.presentation.main.dialogs.EditSampleDialog
import com.set.patchchanger.presentation.main.dialogs.EditSlotDialog
import com.set.patchchanger.presentation.main.dialogs.PerformanceBrowserDialog
import com.set.patchchanger.presentation.main.dialogs.SwapDialog
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.event.UiEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.PatchChangerTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditMode by remember { mutableStateOf(false) }

    // Launchers for File Operations
    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onEvent(MainEvent.SetSampleFile(it, viewModel.getFileName(it))) }
    }

    val libraryPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onEvent(MainEvent.AddFileToLibrary(it, viewModel.getFileName(it))) }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        uri?.let { viewModel.onEvent(MainEvent.PerformExport(it)) }
    }

    val loadFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { viewModel.onEvent(MainEvent.PerformImport(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.RequestFilePicker -> audioPickerLauncher.launch("audio/*")
                is UiEvent.RequestSaveFile -> saveFileLauncher.launch("modx-live-data.json")
                is UiEvent.RequestLoadFile -> loadFileLauncher.launch(arrayOf("application/json"))
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopBar(uiState = uiState, onEvent = viewModel::onEvent) },
        bottomBar = { BottomBar(uiState = uiState, onEvent = viewModel::onEvent, isEditMode = isEditMode) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState) {
                is MainUiState.Success -> {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize().padding(4.dp)) {
                            // Search Bar
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onEvent(MainEvent.UpdateSearchQuery(it)) },
                                label = { Text("Search all patches...") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                                    cursorColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )

                            Spacer(Modifier.height(4.dp))
                            CompactControlsBar(state = uiState, onEvent = viewModel::onEvent)
                            Spacer(Modifier.height(4.dp))
                            CompactSelectorBar(
                                state = uiState,
                                onEvent = viewModel::onEvent,
                                onToggleEdit = { isEditMode = !isEditMode },
                                isEditMode = isEditMode
                            )
                            Spacer(Modifier.height(4.dp))

                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                PatchGrid(
                                    patchData = uiState.patchData,
                                    currentBankIndex = uiState.settings.currentBankIndex,
                                    currentPageIndex = uiState.settings.currentPageIndex,
                                    isEditMode = isEditMode,
                                    onSlotClick = { slot -> viewModel.onEvent(MainEvent.SelectSlot(slot.id)) },
                                    onSlotEdit = { slot -> viewModel.onEvent(MainEvent.ShowSlotColorDialog(slot)) },
                                    onSlotSwap = { sourceId, targetId -> viewModel.onEvent(MainEvent.SwapSlots(sourceId, targetId)) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        SearchResultsOverlay(uiState = uiState, onEvent = viewModel::onEvent)
                    }
                    HandleDialogs(uiState, viewModel, libraryPickerLauncher)
                }
                is MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainUiState.Error -> Text("Error: ${uiState.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun SearchResultsOverlay(uiState: MainUiState.Success, onEvent: (MainEvent) -> Unit) {
    if (uiState.searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty()) {
        Card(
            modifier = Modifier.padding(horizontal = 4.dp).padding(top = 52.dp).fillMaxWidth().heightIn(max = 300.dp).zIndex(10f),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn {
                items(uiState.searchResults) { result ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onEvent(MainEvent.GoToSearchResult(result)) }.padding(16.dp)) {
                        Column {
                            Text(result.slot.getDisplayName(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Bank: ${result.bankName} | Page: ${result.pageName}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
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
    if (uiState.showResetDialog) {
        ConfirmationDialog(
            title = "Reset All Data",
            text = "Are you sure you want to reset all data, including the audio library?",
            onConfirm = { viewModel.onEvent(MainEvent.ResetData) },
            onDismiss = { viewModel.onEvent(MainEvent.ShowResetDialog(false)) }
        )
    }
    if (uiState.showBankPageNameDialog) {
        BankPageNameDialog(
            state = uiState,
            onDismiss = { viewModel.onEvent(MainEvent.ShowBankPageNameDialog(false)) },
            onSaveBank = { viewModel.onEvent(MainEvent.UpdateBankName(uiState.settings.currentBankIndex, it)) },
            onSavePage = { viewModel.onEvent(MainEvent.UpdatePageName(uiState.settings.currentPageIndex, it)) }
        )
    }
    uiState.editingSample?.let { sample ->
        EditSampleDialog(
            sample = sample,
            onDismiss = { viewModel.onEvent(MainEvent.ShowEditSampleDialog(null)) },
            onSave = { viewModel.onEvent(MainEvent.UpdateSample(it)) },
            onLoadFile = { viewModel.onEvent(MainEvent.LoadSampleFile) },
            onSelectFromLibrary = { viewModel.onEvent(MainEvent.ShowAudioLibrary(true, sample.id)) },
            onClearAudio = { viewModel.onEvent(MainEvent.ClearSampleAudio(sample.id)) },
            onEditColor = { viewModel.onEvent(MainEvent.ShowSampleColorDialog(sample)) }
        )
    }
    if (uiState.showAudioLibrary) {
        AudioLibraryDialog(
            library = uiState.audioLibrary,
            onDismiss = { viewModel.onEvent(MainEvent.ShowAudioLibrary(false)) },
            onSelect = { viewModel.onEvent(MainEvent.SelectSampleFromLibrary(it)) },
            onDelete = { viewModel.onEvent(MainEvent.DeleteFromAudioLibrary(it)) },
            onAddFile = { libraryPickerLauncher.launch("audio/*") }
        )
    }
    uiState.slotToPaste?.let { slot ->
        ConfirmationDialog(
            title = "Confirm Paste",
            text = "Paste '${viewModel.internalState.value.slotToPaste?.getDisplayName() ?: "..."}' over '${slot.getDisplayName()}'?",
            onConfirm = { viewModel.onEvent(MainEvent.PasteSlot(slot)) },
            onDismiss = { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(null)) }
        )
    }
    uiState.slotToClear?.let { slot ->
        ConfirmationDialog(
            title = "Clear Slot",
            text = "Are you sure you want to clear slot '${slot.getDisplayName()}'?",
            onConfirm = { viewModel.onEvent(MainEvent.ClearSlot(slot)) },
            onDismiss = { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(null)) }
        )
    }
    uiState.slotToSwap?.let { slot ->
        SwapDialog(
            currentPageSlots = uiState.patchData.banks[uiState.settings.currentBankIndex].pages[uiState.settings.currentPageIndex].slots,
            sourceSlot = slot,
            onDismiss = { viewModel.onEvent(MainEvent.ShowSwapDialog(null)) },
            onSelectSlot = { targetSlot -> viewModel.onEvent(MainEvent.SwapSlots(slot.id, targetSlot.id)) }
        )
    }
    uiState.slotToEditColor?.let { slot ->
        EditSlotDialog(
            slot = slot,
            samples = uiState.samples,
            onDismiss = { viewModel.onEvent(MainEvent.ShowSlotColorDialog(null)) },
            onSave = { viewModel.onEvent(MainEvent.UpdateSlot(it)) },
            onCopy = { viewModel.onEvent(MainEvent.CopySlot(slot)) },
            onPaste = { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(slot)) },
            onSwap = { viewModel.onEvent(MainEvent.ShowSwapDialog(slot)) },
            onClear = { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(slot)) },
            onShowPerformanceBrowser = { viewModel.onEvent(MainEvent.ShowPerformanceBrowser(slot)) }
        )
    }
    uiState.sampleToEditColor?.let { sample ->
        ColorPickerDialog(
            onDismiss = { viewModel.onEvent(MainEvent.ShowSampleColorDialog(null)) },
            onColorSelected = { colorHex ->
                viewModel.onEvent(MainEvent.UpdateSample(sample.copy(color = colorHex)))
                viewModel.onEvent(MainEvent.ShowSampleColorDialog(null))
            }
        )
    }
    if (uiState.showPerformanceBrowser) {
        PerformanceBrowserDialog(uiState = uiState, onEvent = viewModel::onEvent)
    }
}