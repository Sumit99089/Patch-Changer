package com.set.patchchanger.presentation.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.util.Constants
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    uiState: MainUiState,
    onEvent: (MainEvent) -> Unit
) {
    val midiState = (uiState as? MainUiState.Success)?.midiState
    val barColor = if (midiState is MidiConnectionState.Connected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant

    TopAppBar(
        title = {
            Column {
                Text("Live Set Patch Changer", style = MaterialTheme.typography.titleMedium)
                Text("SRIKANTA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor),
        actions = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactControlsBar(
    state: MainUiState.Success,
    onEvent: (MainEvent) -> Unit
) {
    val settings = state.settings
    val midiState = state.midiState

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Transpose
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(-1)) }, modifier = Modifier.size(36.dp)) {
                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (settings.currentTranspose > 0) "+${settings.currentTranspose}" else "${settings.currentTranspose}",
                color = if (settings.currentTranspose != 0) Color(0xFFFFA726) else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onEvent(MainEvent.ResetTranspose) }
                    .padding(horizontal = 4.dp),
                fontSize = 14.sp
            )
            IconButton(onClick = { onEvent(MainEvent.UpdateTranspose(1)) }, modifier = Modifier.size(36.dp)) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Connection Status
        Text(
            text = if (midiState is MidiConnectionState.Connected) midiState.deviceName else "Not Connected",
            color = if (midiState is MidiConnectionState.Connected) Color.Green else Color.Red,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    if (midiState !is MidiConnectionState.Connected) {
                        onEvent(MainEvent.ConnectMidi)
                    }
                }
                .padding(horizontal = 4.dp),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // MIDI Channel Dropdown
        var midiDropdownExpanded by remember { mutableStateOf(false) }
        val midiChannels = (Constants.MIN_MIDI_CHANNEL..Constants.MAX_MIDI_CHANNEL).map { it.toString() }

        Box(
            modifier = Modifier.width(80.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            ExposedDropdownMenuBox(
                expanded = midiDropdownExpanded,
                onExpandedChange = { midiDropdownExpanded = !midiDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = settings.currentMidiChannel.toString(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ch", fontSize = 9.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = midiDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                ExposedDropdownMenu(
                    expanded = midiDropdownExpanded,
                    onDismissRequest = { midiDropdownExpanded = false }
                ) {
                    midiChannels.forEach { channel ->
                        DropdownMenuItem(
                            text = { Text(channel) },
                            onClick = {
                                onEvent(MainEvent.UpdateMidiChannel(channel.toInt()))
                                midiDropdownExpanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactSelectorBar(
    state: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    onToggleEdit: () -> Unit,
    isEditMode: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactSelector(
            label = "Bank",
            value = state.patchData.bankNames.getOrElse(state.settings.currentBankIndex) { "" },
            onPrev = { onEvent(MainEvent.NavigateBank(-1)) },
            onNext = { onEvent(MainEvent.NavigateBank(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier.weight(1f)
        )
        CompactSelector(
            label = "Page",
            value = state.patchData.pageNames.getOrElse(state.settings.currentPageIndex) { "" },
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier.weight(1f)
        )

        // Edit Mode Toggle Button
        FilledTonalButton(
            onClick = onToggleEdit,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (isEditMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier
                .width(60.dp)
                .height(40.dp),
            contentPadding = PaddingValues(0.dp),
            shape = MaterialTheme.shapes.small
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CompactSelector(
    label: String,
    value: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrev,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.ArrowUpward, null, modifier = Modifier.size(20.dp))
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.small
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.ArrowDownward, null, modifier = Modifier.size(20.dp))
        }
    }
}