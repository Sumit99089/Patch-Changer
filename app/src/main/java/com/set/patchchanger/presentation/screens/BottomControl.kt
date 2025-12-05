package com.set.patchchanger.presentation.main.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBar(
    uiState: MainUiState,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean
) {
    val samples = (uiState as? MainUiState.Success)?.samples ?: emptyList()

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 2.dp)
    ) {
        // Sample Pads
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            samples.take(4).forEach { sample ->
                SampleButton(
                    sample = sample,
                    isEditMode = isEditMode,
                    onEvent = onEvent
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // Bottom Controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEvent(MainEvent.RequestExportData) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Save, "Save", modifier = Modifier.width(18.dp))
                }
                IconButton(onClick = { onEvent(MainEvent.RequestImportData) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FolderOpen, "Load", modifier = Modifier.width(18.dp))
                }
                TextButton(
                    onClick = { onEvent(MainEvent.ShowResetDialog(true)) },
                    modifier = Modifier.size(60.dp, 36.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("X Reset", color = Color.Red, fontSize = 9.sp)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Placeholder for Settings
                IconButton(onClick = { /* TODO: Show Settings Dialog */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Settings, "Settings", modifier = Modifier.width(18.dp))
                }
                // Placeholder for Power
                IconButton(onClick = { /* TODO: Show Power/Quit Dialog */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.PowerSettingsNew, "Power", modifier = Modifier.width(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.SampleButton(
    sample: SamplePad,
    isEditMode: Boolean,
    onEvent: (MainEvent) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val baseColor = try {
        Color(sample.color.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Visual feedback logic
    val containerColor = if (isPressed) Color.White else baseColor
    val contentColor = if (isPressed) baseColor else Color.White
    val borderColor = if (isPressed) baseColor else Color.Transparent

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 1.dp)
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(2.dp, borderColor, RoundedCornerShape(6.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple to use our custom color swap
                onClick = {
                    if (isEditMode) onEvent(MainEvent.ShowEditSampleDialog(sample))
                    else onEvent(MainEvent.TriggerSample(sample.id))
                },
                onLongClick = {
                    onEvent(MainEvent.ShowEditSampleDialog(sample))
                }
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            Text(
                text = sample.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
                fontSize = 10.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}