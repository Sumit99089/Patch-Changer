package com.set.patchchanger.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.*

@Composable
fun AppTopBar(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean,
    onToggleEdit: () -> Unit
) {
    val midiState = uiState.midiState
    val settings = uiState.settings

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFF151515)) // Slightly darker for the bar
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Title Section
        Column(modifier = Modifier.width(180.dp)) {
            Text(
                "Live Set Patch Changer",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "SRIKANTA",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        // 2. Search Bar (Center)
        Box(modifier = Modifier.weight(1f)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(MainEvent.UpdateSearchQuery(it)) },
                placeholder = { Text("Search all patches...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                textStyle = TextStyle(fontSize = 12.sp, color = TextPrimary),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BorderColor,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    cursorColor = ColorBlueTitle
                ),
                shape = RoundedCornerShape(4.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        // 3. Right Controls
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            // Transpose Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(DarkSurface, RoundedCornerShape(4.dp)).padding(2.dp)
            ) {
                Text("Transpose", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                IconButton(
                    onClick = { onEvent(MainEvent.UpdateTranspose(-1)) },
                    modifier = Modifier.size(30.dp).background(BorderColor, RoundedCornerShape(4.dp))
                ) { Text("-", color = TextPrimary, fontWeight = FontWeight.Bold) }

                Text(
                    text = if (settings.currentTranspose > 0) "+${settings.currentTranspose}" else "${settings.currentTranspose}",
                    color = ColorYellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(30.dp).clickable { onEvent(MainEvent.ResetTranspose) },
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onEvent(MainEvent.UpdateTranspose(1)) },
                    modifier = Modifier.size(30.dp).background(BorderColor, RoundedCornerShape(4.dp))
                ) { Text("+", color = TextPrimary, fontWeight = FontWeight.Bold) }
            }

            // MIDI Status Text
            Text(
                text = if (midiState is MidiConnectionState.Connected) midiState.deviceName else "MIDI Access Denied",
                color = if (midiState is MidiConnectionState.Connected) ColorGreen else ColorRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onEvent(MainEvent.ConnectMidi) }
            )

            // MIDI Channel Dropdown
            var midiMenuExpanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { midiMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if(midiState is MidiConnectionState.Connected) ColorGreen else ColorRed),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("MIDI", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(4.dp))
                    Text(settings.currentMidiChannel.toString(), color = Color.Black, fontSize = 10.sp)
                    Icon(Icons.Default.ArrowDropDown, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(
                    expanded = midiMenuExpanded,
                    onDismissRequest = { midiMenuExpanded = false }
                ) {
                    (1..16).forEach { ch ->
                        DropdownMenuItem(
                            text = { Text("Channel $ch") },
                            onClick = {
                                onEvent(MainEvent.UpdateMidiChannel(ch))
                                midiMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Edit Button
            Button(
                onClick = onToggleEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditMode) ColorOrange else DarkSurface,
                    contentColor = if (isEditMode) Color.Black else TextPrimary
                ),
                border = if(!isEditMode) androidx.compose.foundation.BorderStroke(1.dp, BorderColor) else null,
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isEditMode) "Done" else "Edit", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SelectorBar(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bank Selector (Left)
        SelectorItem(
            label = "Bank",
            value = uiState.patchData.bankNames.getOrElse(uiState.settings.currentBankIndex) { "User 1" },
            onPrev = { onEvent(MainEvent.NavigateBank(-1)) },
            onNext = { onEvent(MainEvent.NavigateBank(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier.weight(1f).padding(end = 4.dp)
        )

        // Page Selector (Right)
        SelectorItem(
            label = "Page",
            value = uiState.patchData.pageNames.getOrElse(uiState.settings.currentPageIndex) { "Page 1" },
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier.weight(1f).padding(start = 4.dp)
        )
    }
}

@Composable
fun SelectorItem(
    label: String,
    value: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Up Arrow
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(36.dp).clickable(onClick = onPrev),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Icon(Icons.Default.ArrowDropUp, null, tint = TextPrimary, modifier = Modifier.padding(4.dp))
        }

        // Display Box
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .padding(horizontal = 4.dp)
                .background(Color(0xFF222222), RoundedCornerShape(4.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(4.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, color = TextSecondary, fontSize = 8.sp)
                Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
            }
        }

        // Down Arrow
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.size(36.dp).clickable(onClick = onNext),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
        ) {
            Icon(Icons.Default.ArrowDropDown, null, tint = TextPrimary, modifier = Modifier.padding(4.dp))
        }
    }
}