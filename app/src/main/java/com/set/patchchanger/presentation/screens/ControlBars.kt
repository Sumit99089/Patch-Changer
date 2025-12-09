package com.set.patchchanger.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.ColorGreen
import com.set.patchchanger.ui.theme.ColorOrange
import com.set.patchchanger.ui.theme.ColorRed
import com.set.patchchanger.ui.theme.ColorYellow

@Composable
fun AppTopBar(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean,
    onToggleEdit: () -> Unit
) {
    val midiState = uiState.midiState
    val settings = uiState.settings
    // Use theme surface color
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(surfaceColor)
            .border(width = 1.dp, color = outlineColor)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Title
        Column(modifier = Modifier.wrapContentWidth()) {
            Text(
                "Sonic Grid",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                "@beat0",
                modifier = Modifier.align(Alignment.End),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 12.5.sp
            )
        }

        Spacer(Modifier.width(24.dp))

        // 2. Search
        Box(modifier = Modifier.weight(1f)) {
            var isFocused by remember { mutableStateOf(false) }
            BasicTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(MainEvent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                    .border(
                        1.dp,
                        if (isFocused) Color(0xFF448AFF) else outlineColor,
                        RoundedCornerShape(4.dp)
                    )
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                singleLine = true,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { inner ->
                    Box {
                        if (uiState.searchQuery.isEmpty()) Text(
                            "Search all patches...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        inner()
                    }
                }
            )
        }

        Spacer(Modifier.width(24.dp))

        // 3. Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Transpose
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
                Text(
                    "Transpose",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Animation Logic for buttons
                // HTML: .blinking { animation: blink-orange 1.2s infinite; } -> Orange to Dark Orange
                // Here we use Theme Primary to Secondary for generic blinking, or explicit colors
                val blinkColor1 = ColorOrange
                val blinkColor2 = Color(0xFFE65100) // Deep Orange

                // Minus Button
                val isMinusBlinking = settings.currentTranspose < 0
                val minusColor by animateColorAsState(
                    targetValue = if (isMinusBlinking) blinkColor1 else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "minus"
                )

                Box(
                    modifier = Modifier
                        .size(32.dp, 30.dp)
                        .background(
                            minusColor,
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        )
                        .clickable { onEvent(MainEvent.UpdateTranspose(-1)) }
                        .border(
                            1.dp,
                            outlineColor,
                            RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "-",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Value
                Box(
                    modifier = Modifier
                        .size(32.dp, 30.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .clickable { onEvent(MainEvent.ResetTranspose) }
                        .border(1.dp, outlineColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${settings.currentTranspose}",
                        color = if (settings.currentTranspose != 0) ColorYellow else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Plus Button
                val isPlusBlinking = settings.currentTranspose > 0
                val plusColor by animateColorAsState(
                    targetValue = if (isPlusBlinking) blinkColor1 else MaterialTheme.colorScheme.surfaceVariant,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                    label = "plus"
                )

                Box(
                    modifier = Modifier
                        .size(32.dp, 30.dp)
                        .background(plusColor, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                        .clickable { onEvent(MainEvent.UpdateTranspose(1)) }
                        .border(
                            1.dp,
                            outlineColor,
                            RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "+",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // MIDI Status
            Text(
                text = if (midiState is MidiConnectionState.Connected) midiState.deviceName else "Not Connected",
                color = if (midiState is MidiConnectionState.Connected) ColorGreen else ColorRed,
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onEvent(MainEvent.ConnectMidi) }
            )

            // MIDI Channel
            var midiMenuExpanded by remember { mutableStateOf(false) }
            Box {
                Button(
                    onClick = { midiMenuExpanded = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (midiState is MidiConnectionState.Connected) ColorGreen else ColorRed),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
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
                    onDismissRequest = { midiMenuExpanded = false }) {
                    (1..16).forEach { ch ->
                        DropdownMenuItem(
                            text = { Text("Channel $ch") },
                            onClick = {
                                onEvent(MainEvent.UpdateMidiChannel(ch)); midiMenuExpanded = false
                            })
                    }
                }
            }

            // Edit Button
            Button(
                onClick = onToggleEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditMode) ColorOrange else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isEditMode) Color.Black else MaterialTheme.colorScheme.onSurface
                ),
                border = if (!isEditMode) androidx.compose.foundation.BorderStroke(
                    1.dp,
                    outlineColor
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
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean
) {
    val bankName =
        uiState.patchData.bankNames.getOrElse(uiState.settings.currentBankIndex) { "User 1" }
    val pageName =
        uiState.patchData.pageNames.getOrElse(uiState.settings.currentPageIndex) { "Page 1" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Bank Selector
        SelectorItem(
            label = "Bank", value = bankName,
            onPrev = { onEvent(MainEvent.NavigateBank(-1)) },
            onNext = { onEvent(MainEvent.NavigateBank(1)) },
            // FIX: Restrict click to Edit Mode only
            onClick = { if (isEditMode) onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Page Selector
        SelectorItem(
            label = "Page", value = pageName,
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
            // FIX: Restrict click to Edit Mode only
            onClick = { if (isEditMode) onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp)
        )
    }
}

@Composable
fun SelectorItem(
    label: String, value: String,
    onPrev: () -> Unit, onNext: () -> Unit, onClick: () -> Unit,
    modifier: Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(38.dp)
                .clickable(onClick = onPrev),
            border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowDropUp,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .background(surfaceColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .border(1.dp, outlineColor, RoundedCornerShape(4.dp))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.sp,
                    lineHeight = 10.sp
                )
                Text(
                    value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(38.dp)
                .clickable(onClick = onNext),
            border = androidx.compose.foundation.BorderStroke(1.dp, outlineColor)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}