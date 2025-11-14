package com.set.patchchanger.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.set.patchchanger.domain.model.AppTheme
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
    var isEditMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Launchers
    val audioPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onEvent(MainEvent.SetSampleFile(it, viewModel.getFileName(it))) }
    }
    val libraryPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.onEvent(MainEvent.AddFileToLibrary(it, viewModel.getFileName(it))) }
    }

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
                    Column(Modifier.fillMaxSize().padding(4.dp)) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search all patches...") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
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

                        // Patch Grid
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

                    // --- Dialogs (Calling Logic) ---
                    // This function now contains all the dialogs from the original file.
                    HandleDialogs(uiState, viewModel, libraryPickerLauncher)
                }

                is MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainUiState.Error -> Text("Error: ${uiState.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun HandleDialogs(
    uiState: MainUiState.Success,
    viewModel: MainViewModel,
    libraryPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
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
            onSaveBank = {
                viewModel.onEvent(
                    MainEvent.UpdateBankName(
                        uiState.settings.currentBankIndex,
                        it
                    )
                )
            },
            onSavePage = {
                viewModel.onEvent(
                    MainEvent.UpdatePageName(
                        uiState.settings.currentPageIndex,
                        it
                    )
                )
            }
        )
    }

    uiState.editingSample?.let { sample ->
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
            onSelectSlot = { targetSlot ->
                viewModel.onEvent(MainEvent.SwapSlots(slot.id, targetSlot.id))
            }
        )
    }

    uiState.slotToEditColor?.let { slot ->
        EditSlotDialog(
            slot = slot,
            onDismiss = { viewModel.onEvent(MainEvent.ShowSlotColorDialog(null)) },
            onSave = { viewModel.onEvent(MainEvent.UpdateSlot(it)) },
            onCopy = { viewModel.onEvent(MainEvent.CopySlot(slot)) },
            onPaste = { viewModel.onEvent(MainEvent.ShowPasteConfirmDialog(slot)) },
            onSwap = { viewModel.onEvent(MainEvent.ShowSwapDialog(slot)) },
            onClear = { viewModel.onEvent(MainEvent.ShowClearConfirmDialog(slot)) },
            samples = uiState.samples
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
}