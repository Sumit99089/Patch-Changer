package com.set.patchchanger.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.SearchResult
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.event.UiEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.DarkBackground
import com.set.patchchanger.ui.theme.PatchChangerTheme

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onToggleFullscreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTheme = (uiState as? MainUiState.Success)?.settings?.theme ?: AppTheme.BLACK

    PatchChangerTheme(appTheme = currentTheme) {
        MainScreenContent(viewModel, uiState, onToggleFullscreen)
    }
}

@Composable
fun MainScreenContent(
    viewModel: MainViewModel,
    uiState: MainUiState,
    onToggleFullscreen: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var isEditMode by remember { mutableStateOf(false) }

    // Launchers for File Operations
    val audioPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { viewModel.onEvent(MainEvent.SetSampleFile(it, viewModel.getFileName(it))) }
        }

    val libraryPickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                viewModel.onEvent(
                    MainEvent.AddFileToLibrary(
                        it,
                        viewModel.getFileName(it)
                    )
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
        containerColor = DarkBackground,
        contentColor = Color.White
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBackground)
        ) {
            when (uiState) {
                is MainUiState.Success -> {
                    Column(Modifier.fillMaxSize()) {

                        // 1. Top Bar
                        AppTopBar(uiState, viewModel::onEvent, isEditMode) {
                            isEditMode = !isEditMode
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            Column(Modifier.fillMaxSize()) {
                                // 2. Selector Bar
                                SelectorBar(uiState, viewModel::onEvent)

                                // 3. Grid
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

                            // Search Overlay
                            if (uiState.searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty()) {
                                SearchResultsOverlay(uiState, viewModel::onEvent)
                            }
                        }

                        // 4. Bottom Bar
                        BottomBar(uiState, viewModel::onEvent, isEditMode, onToggleFullscreen)
                    }

                    // Dialogs
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

@Composable
fun SearchResultsOverlay(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(top = 0.dp, start = 200.dp, end = 200.dp) // Centered under search bar approx
            .fillMaxWidth(0.5f)
            .heightIn(max = 300.dp)
            .zIndex(100f),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = com.set.patchchanger.ui.theme.DarkSurface)
    ) {
        LazyColumn {
            items(uiState.searchResults) { result ->
                SearchResultItem(
                    result = result,
                    onClick = { onEvent(MainEvent.GoToSearchResult(result)) }
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.slot.getDisplayName(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )
            Text(
                text = "Bank: ${result.bankName}  |  Page: ${result.pageName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}