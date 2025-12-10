package com.set.patchchanger.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.event.UiEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.PatchChangerTheme

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

@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    uiState: MainUiState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditMode by remember { mutableStateOf(false) }

    val audioPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.onEvent(MainEvent.SetSampleFile(it, viewModel.getFileName(it))) }
        }
    val libraryPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.onEvent(
                    MainEvent.AddFileToLibrary(it, viewModel.getFileName(it))
                )
            }
        }
    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
            uri?.let { viewModel.onEvent(MainEvent.PerformExport(it)) }
        }
    val loadFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { viewModel.onEvent(MainEvent.PerformImport(it)) }
        }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
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
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (uiState) {
                is MainUiState.Success -> {
                    Column(Modifier.fillMaxSize()) {
                        // 1. Top Bar (Contains Search Dropdown Logic now)
                        AppTopBar(uiState, viewModel::onEvent, isEditMode) {
                            isEditMode = !isEditMode
                        }

                        // 2. Main Content (Selector + Grid)
                        Box(modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxSize()) {
                                SelectorBar(uiState, viewModel::onEvent, isEditMode)

                                PatchGrid(
                                    patchData = uiState.patchData,
                                    currentBankIndex = uiState.settings.currentBankIndex,
                                    currentPageIndex = uiState.settings.currentPageIndex,
                                    isEditMode = isEditMode,
                                    onSlotClick = { slot ->
                                        viewModel.onEvent(
                                            MainEvent.SelectSlot(
                                                slot.id
                                            )
                                        )
                                    },
                                    onSlotEdit = { slot ->
                                        viewModel.onEvent(
                                            MainEvent.ShowSlotColorDialog(
                                                slot
                                            )
                                        )
                                    },
                                    onSlotSwap = { sourceId, targetId ->
                                        viewModel.onEvent(
                                            MainEvent.SwapSlots(
                                                sourceId,
                                                targetId
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // 3. Bottom Bar
                        BottomBar(uiState, viewModel::onEvent, isEditMode)
                    }

                    HandleDialogs(uiState, viewModel, libraryPickerLauncher)
                }

                is MainUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MainUiState.Error -> Text(
                    "Error: ${uiState.message}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}