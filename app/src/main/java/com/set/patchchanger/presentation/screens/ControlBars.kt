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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.BorderColor
import com.set.patchchanger.ui.theme.ColorBlueTitle
import com.set.patchchanger.ui.theme.ColorGreen
import com.set.patchchanger.ui.theme.ColorOrange
import com.set.patchchanger.ui.theme.ColorRed
import com.set.patchchanger.ui.theme.ColorYellow
import com.set.patchchanger.ui.theme.DarkBackground
import com.set.patchchanger.ui.theme.DarkSurface
import com.set.patchchanger.ui.theme.TextHint
import com.set.patchchanger.ui.theme.TextPrimary
import com.set.patchchanger.ui.theme.TextSecondary

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
            .height(56.dp)
            .background(DarkSurface)
            .border(width = 1.dp, color = Color(0xFF2A2E35))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Title Section
        Column(modifier = Modifier.wrapContentWidth()) {
            Text(
                text = "Sonic Grid",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                modifier = Modifier.align(Alignment.End),
                text = "@beat0", // UserName
                color = TextSecondary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(24.dp))

        // 2. Search Bar (Center) - Replaced with BasicTextField for perfect height control
        Box(modifier = Modifier.weight(1f)) {
            var isFocused by remember { mutableStateOf(false) }

            BasicTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(MainEvent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color(0xFF15181E), RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = if (isFocused) Color(0xFF448AFF) else BorderColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(horizontal = 6.dp), // Internal padding
                textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                singleLine = true,
                cursorBrush = SolidColor(ColorBlueTitle),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                "Search all patches...",
                                color = TextHint,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(Modifier.width(24.dp))

        // 3. Right Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Transpose Control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    "Transpose",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Minus Button
                Box(
                    modifier = Modifier
                        .size(32.dp, 30.dp)
                        .background(
                            Color(0xFF252930),
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        )
                        .clickable { onEvent(MainEvent.UpdateTranspose(-1)) }
                        .border(
                            1.dp,
                            BorderColor,
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                // Value
                Box(
                    modifier = Modifier
                        .size(32.dp, 30.dp)
                        .background(Color(0xFF101216))
                        .clickable { onEvent(MainEvent.ResetTranspose) }
                        .border(1.dp, BorderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${settings.currentTranspose}",
                        color = if (settings.currentTranspose != 0) ColorYellow else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Plus Button
                Box(
                    modifier = Modifier
                        .size(32.dp, 30.dp)
                        .background(
                            Color(0xFF252930),
                            RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        )
                        .clickable { onEvent(MainEvent.UpdateTranspose(1)) }
                        .border(
                            1.dp,
                            BorderColor,
                            RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // MIDI Status Text
            Text(
                text = if (midiState is MidiConnectionState.Connected) midiState.deviceName else "MIDI Access Denied",
                color = if (midiState is MidiConnectionState.Connected) ColorGreen else ColorRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onEvent(MainEvent.ConnectMidi) }
            )

            // MIDI Channel Dropdown
            var midiMenuExpanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { midiMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (midiState is MidiConnectionState.Connected) ColorRed else ColorRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("MIDI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        settings.currentMidiChannel.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp))
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
                    containerColor = if (isEditMode) ColorOrange else Color(0xFF252930),
                    contentColor = if (isEditMode) Color.Black else TextPrimary
                ),
                border = if (!isEditMode) androidx.compose.foundation.BorderStroke(
                    1.dp,
                    BorderColor
                ) else null,
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
            .background(DarkBackground)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bank Selector (Left)
        SelectorItem(
            label = "Bank",
            value = uiState.patchData.bankNames.getOrElse(uiState.settings.currentBankIndex) { "User 1" },
            onPrev = { onEvent(MainEvent.NavigateBank(-1)) },
            onNext = { onEvent(MainEvent.NavigateBank(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Page Selector (Right)
        SelectorItem(
            label = "Page",
            value = uiState.patchData.pageNames.getOrElse(uiState.settings.currentPageIndex) { "Page 1" },
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
            onClick = { onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
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
            color = Color(0xFF1E242E),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(height = 38.dp, width = 38.dp)
                .clickable(onClick = onPrev),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38404B))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowDropUp,
                    null,
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // Display Box
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .background(Color(0xFF0D0F12), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFF38404B), RoundedCornerShape(4.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    label,
                    color = TextSecondary,
                    fontSize = 9.sp,
                    lineHeight = 10.sp
                )
                Text(
                    value,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // Down Arrow
        Surface(
            color = Color(0xFF1E242E),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(height = 38.dp, width = 38.dp)
                .clickable(onClick = onNext),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38404B))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}