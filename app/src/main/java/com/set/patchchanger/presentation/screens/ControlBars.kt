package com.set.patchchanger.presentation.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.set.patchchanger.domain.model.MidiConnectionState
import com.set.patchchanger.domain.model.SearchResult
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
    val surfaceColor = MaterialTheme.colorScheme.surface
    val outlineColor = MaterialTheme.colorScheme.outline

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .border(width = 1.dp, color = outlineColor)
    ) {
        // Breakpoint for Portrait mode (less than 600dp width usually implies phone portrait)
        if (maxWidth < 600.dp) {
            AppTopBarPortrait(uiState, onEvent, isEditMode, onToggleEdit)
        } else {
            AppTopBarLandscape(uiState, onEvent, isEditMode, onToggleEdit)
        }
    }
}

@Composable
fun AppTopBarLandscape(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean,
    onToggleEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Title
        TopBarTitle()

        Spacer(Modifier.width(24.dp))

        // 2. Search
        Box(modifier = Modifier.weight(1f)) {
            TopBarSearch(uiState, onEvent)
        }

        Spacer(Modifier.width(24.dp))

        // 3. Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopBarTranspose(uiState, onEvent)
            TopBarMidiStatus(uiState, onEvent)
            TopBarEditButton(isEditMode, onToggleEdit)
        }
    }
}

@Composable
fun AppTopBarPortrait(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean,
    onToggleEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Title and Edit Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarTitle()
            TopBarEditButton(isEditMode, onToggleEdit)
        }

        // Row 2: Search Bar
        Box(modifier = Modifier.fillMaxWidth()) {
            TopBarSearch(uiState, onEvent)
        }

        // Row 3: Transpose and Midi Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarTranspose(uiState, onEvent)
            TopBarMidiStatus(uiState, onEvent)
        }
    }
}

// --- Sub-Components for Reusability ---

@Composable
fun TopBarTitle() {
    Column(modifier = Modifier.wrapContentWidth()) {
        Text(
            text = "Live Set Patch Changer",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun TopBarSearch(uiState: MainUiState.Success, onEvent: (MainEvent) -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
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
                    "Search ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                inner()
            }
        }
    )

    // Results Popup
    if (uiState.searchQuery.isNotBlank() && uiState.searchResults.isNotEmpty()) {
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(0, 100)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .border(1.dp, outlineColor, RoundedCornerShape(0.dp, 0.dp, 6.dp, 6.dp)),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp, 0.dp, 6.dp, 6.dp)
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
    }
}

@Composable
fun TopBarTranspose(uiState: MainUiState.Success, onEvent: (MainEvent) -> Unit) {
    val settings = uiState.settings
    val outlineColor = MaterialTheme.colorScheme.outline

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(36.dp)) {
        Text(
            "Transpose",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Shared Infinite Transition for Blinking
        val infiniteTransition = rememberInfiniteTransition(label = "transpose_blink")
        val blinkColor by infiniteTransition.animateColor(
            initialValue = MaterialTheme.colorScheme.surfaceVariant,
            targetValue = ColorOrange,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blink_color"
        )

        val minusBg =
            if (settings.currentTranspose < 0) blinkColor else MaterialTheme.colorScheme.surfaceVariant

        Box(
            modifier = Modifier
                .size(32.dp, 30.dp)
                .background(
                    minusBg,
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

        val plusBg =
            if (settings.currentTranspose > 0) blinkColor else MaterialTheme.colorScheme.surfaceVariant

        Box(
            modifier = Modifier
                .size(32.dp, 30.dp)
                .background(plusBg, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
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
}

@Composable
fun TopBarMidiStatus(uiState: MainUiState.Success, onEvent: (MainEvent) -> Unit) {
    val midiState = uiState.midiState
    val settings = uiState.settings

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // MIDI Status
        val midiTransition = rememberInfiniteTransition(label = "midi_blink")
        val midiBlinkColor by midiTransition.animateColor(
            initialValue = Color.Transparent,
            targetValue = if (midiState is MidiConnectionState.Connected) ColorGreen else ColorRed,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse
            ),
            label = "midi_state_blink"
        )

        val finalMidiColor =
            if (midiState is MidiConnectionState.Connected) midiBlinkColor else midiBlinkColor

        Text(
            text = if (midiState is MidiConnectionState.Connected) midiState.deviceName else "Not Connected",
            color = if (midiState is MidiConnectionState.Connected) Color.Green else Color.Red,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        // MIDI Channel Button
        var midiMenuExpanded by remember { mutableStateOf(false) }
        Box {
            Button(
                onClick = { midiMenuExpanded = true },
                colors = ButtonDefaults.buttonColors(containerColor = finalMidiColor),
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
    }
}

@Composable
fun TopBarEditButton(isEditMode: Boolean, onToggleEdit: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.outline
    Button(
        onClick = onToggleEdit,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEditMode) ColorOrange else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isEditMode) Color.Black else MaterialTheme.colorScheme.onSurface
        ),
        border = if (!isEditMode) BorderStroke(1.dp, outlineColor) else null,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = Modifier.height(30.dp)
    ) {
        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(if (isEditMode) "Done" else "Edit", fontSize = 12.sp)
    }
}


@Composable
fun SearchResultItem(result: SearchResult, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                result.slot.getDisplayName(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Bank: ${result.bankName}  |  Page: ${result.pageName} / Slot ${result.slot.getSlotNumber()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
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
        SelectorItem(
            label = "Bank", value = bankName,
            onPrev = { onEvent(MainEvent.NavigateBank(-1)) },
            onNext = { onEvent(MainEvent.NavigateBank(1)) },
            onClick = { if (isEditMode) onEvent(MainEvent.ShowBankPageNameDialog(true)) },
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        )

        Spacer(Modifier.width(8.dp))

        SelectorItem(
            label = "Page", value = pageName,
            onPrev = { onEvent(MainEvent.NavigatePage(-1)) },
            onNext = { onEvent(MainEvent.NavigatePage(1)) },
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
    val skyBlueRipple = ripple(color = Color(0xFF87CEEB))

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .size(38.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = skyBlueRipple,
                    onClick = onPrev
                ),
            border = BorderStroke(1.dp, outlineColor)
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = skyBlueRipple,
                    onClick = onNext
                ),
            border = BorderStroke(1.dp, outlineColor)
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