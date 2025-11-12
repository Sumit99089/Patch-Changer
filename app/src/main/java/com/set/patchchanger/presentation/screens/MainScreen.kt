package com.set.patchchanger.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.PatchData
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.presentation.viewmodel.MainEvent
import com.set.patchchanger.presentation.viewmodel.MainUiState
import com.set.patchchanger.presentation.viewmodel.MainViewModel
import com.set.patchchanger.presentation.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

/**
 * Main Screen Composable
 *
 * Jetpack Compose builds UI declaratively.
 * UI is a function of state: UI = f(state)
 *
 * When state changes, Compose automatically recomposes
 * affected UI elements.
 *
 * @Composable annotation marks a function as a Compose UI function
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    /**
     * collectAsState() converts Flow to Compose State.
     * When Flow emits, UI recomposes.
     */
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    /**
     * LaunchedEffect runs when key changes.
     * Used for one-time events like snackbars.
     */
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.PlaySample -> {
                    // Play sample audio
                    playSample(context, event.sampleId)
                }
            }
        }
    }

    /**
     * Scaffold provides Material Design structure
     * (top bar, bottom bar, content, etc.)
     */
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        },
        bottomBar = {
            BottomBar(
                uiState = uiState,
                onEvent = viewModel::onEvent
            )
        }
    ) { padding ->
        /**
         * Main content based on UI state
         */
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is MainUiState.Success -> {
                    MainContent(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }

                is MainUiState.Error -> {
                    ErrorView(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

/**
 * Top Bar with search, transpose, and MIDI status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    uiState: MainUiState,
    onEvent: (MainEvent) -> Unit
) {
    val settings = (uiState as? MainUiState.Success)?.settings
    val midiState = (uiState as? MainUiState.Success)?.midiState

    TopAppBar(
        title = {
            Column {
                Text("Live Set Patch Changer", style = MaterialTheme.typography.titleMedium)
                Text("SRIKANTA", style = MaterialTheme.typography.bodySmall)
            }
        },
        actions = {
            // Transpose controls
            settings?.let { s ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Transpose", style = MaterialTheme.typography.labelSmall)

                    IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(-1)) }) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }

                    Text(
                        text = if (s.currentTranspose > 0) "+${s.currentTranspose}"
                        else s.currentTranspose.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onEvent(MainEvent.ResetTranspose) }
                            .padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(1)) }) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
            }

            // MIDI Status
            midiState?.let { state ->
                IconButton(onClick = {
                    when (state) {
                        is MidiConnectionState.Connected -> onEvent(MainEvent.DisconnectMidi)
                        else -> onEvent(MainEvent.ConnectMidi)
                    }
                }) {
                    Badge(
                        containerColor = when (state) {
                            is MidiConnectionState.Connected -> Color.Green
                            else -> Color.Red
                        }
                    ) {
                        Icon(Icons.Default.MusicNote, "MIDI")
                    }
                }
            }
        }
    )
}

/**
 * Main content area with bank/page selectors and patch grid
 */
@Composable
fun MainContent(
    state: MainUiState.Success,
    onEvent: (MainEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Bank/Page selectors
        SelectorBar(
            state = state,
            onEvent = onEvent
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Patch grid
        PatchGrid(
            patchData = state.patchData,
            currentBankIndex = state.settings.currentBankIndex,
            currentPageIndex = state.settings.currentPageIndex,
            onSlotClick = { slotId ->
                onEvent(MainEvent.SelectSlot(slotId))
            }
        )
    }
}

/**
 * Bank and Page selector bar
 */
@Composable
fun SelectorBar(
    state: MainUiState.Success,
    onEvent: (MainEvent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Bank selector
        SelectorGroup(
            label = "Bank",
            value = state.patchData.bankNames[state.settings.currentBankIndex],
            onUpClick = { onEvent(MainEvent.NavigateBank(-1)) },
            onDownClick = { onEvent(MainEvent.NavigateBank(1)) },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Page selector
        SelectorGroup(
            label = "Page",
            value = state.patchData.pageNames[state.settings.currentPageIndex],
            onUpClick = { onEvent(MainEvent.NavigatePage(-1)) },
            onDownClick = { onEvent(MainEvent.NavigatePage(1)) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Reusable selector group (up button, label, down button)
 */
@Composable
fun SelectorGroup(
    label: String,
    value: String,
    onUpClick: () -> Unit,
    onDownClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onUpClick) {
            Icon(Icons.Default.ArrowUpward, "Previous")
        }

        Card(
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(onClick = onDownClick) {
            Icon(Icons.Default.ArrowDownward, "Next")
        }
    }
}

/**
 * 4x4 Grid of patch slots
 */
@Composable
fun PatchGrid(
    patchData: PatchData,
    currentBankIndex: Int,
    currentPageIndex: Int,
    onSlotClick: (Int) -> Unit
) {
    val currentPage = patchData.banks
        .getOrNull(currentBankIndex)
        ?.pages
        ?.getOrNull(currentPageIndex)

    currentPage?.let { page ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(page.slots) { slot ->
                PatchSlotCard(
                    slot = slot,
                    onClick = { onSlotClick(slot.id) }
                )
            }
        }
    }
}

/**
 * Individual patch slot card
 */
@Composable
fun PatchSlotCard(
    slot: PatchSlot,
    onClick: () -> Unit
) {
    /**
     * Parse hex color string to Color
     */
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(slot.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = if (slot.selected) {
            BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = slot.getDisplayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Bottom bar with sample pads and controls
 */
@Composable
fun BottomBar(
    uiState: MainUiState,
    onEvent: (MainEvent) -> Unit
) {
    val samples = (uiState as? MainUiState.Success)?.samples ?: return

    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            samples.forEach { sample ->
                SamplePadButton(
                    sample = sample,
                    onClick = { /* Play sample */ }
                )
            }
        }
    }
}

/**
 * Sample pad button
 */
@Composable
fun SamplePadButton(
    sample: SamplePad,
    onClick: () -> Unit
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(sample.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Button(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        )
    ) {
        Text(
            text = sample.name,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Error view
 */
@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Helper function to play sample audio
 */
private fun playSample(context: android.content.Context, sampleId: Int) {
    // Implementation would use MediaPlayer or SoundPool
    // to play the audio file associated with this sample
}