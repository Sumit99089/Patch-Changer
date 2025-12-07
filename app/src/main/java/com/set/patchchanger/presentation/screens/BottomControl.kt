package com.set.patchchanger.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.DarkBackground
import com.set.patchchanger.ui.theme.SampleGreen
import com.set.patchchanger.ui.theme.SamplePink
import com.set.patchchanger.ui.theme.SamplePurple
import com.set.patchchanger.ui.theme.SampleTeal
import com.set.patchchanger.ui.theme.TextPrimary
import com.set.patchchanger.ui.theme.TextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBar(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean,
    onToggleFullscreen: () -> Unit
) {
    val samples = uiState.samples

    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp) // Matched height
            .background(DarkBackground) // Seamless dark background
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Data Management (Left)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DataButton(
                "Save",
                Icons.Default.Save,
                Color(0xFF2E3440)
            ) { onEvent(MainEvent.RequestExportData) }
            DataButton(
                "Load",
                Icons.Default.FolderOpen,
                Color(0xFF2E3440)
            ) { onEvent(MainEvent.RequestImportData) }
            DataButton(
                "Reset",
                Icons.Default.Close,
                Color(0xFFE57373)
            ) { onEvent(MainEvent.ShowResetDialog(true)) }
        }

        Spacer(Modifier.width(8.dp))

        // 2. Sample Pads (Center - Weight)
        // Hardcoded precise screenshot colors
        val defaultColors = listOf(SampleTeal, SamplePink, SampleGreen, SamplePurple)

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0..3) {
                val sample = samples.getOrNull(i)
                val dbColor = sample?.color

                // If the DB color is the default grey/black, override with screenshot colors
                val displayColor =
                    if (dbColor != null && dbColor != "#008B8B" && dbColor != "#F50057" && dbColor != "#00C853" && dbColor != "#D500F9") {
                        try {
                            Color(dbColor.toColorInt())
                        } catch (e: Exception) {
                            defaultColors[i]
                        }
                    } else {
                        defaultColors[i]
                    }

                SampleButton(
                    name = sample?.name ?: "S${i + 1}",
                    color = displayColor,
                    isEditMode = isEditMode,
                    onClick = { if (sample != null) onEvent(MainEvent.TriggerSample(sample.id)) },
                    onLongClick = {
                        if (sample != null) onEvent(
                            MainEvent.ShowEditSampleDialog(
                                sample
                            )
                        )
                    }
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // 3. Bottom Right Controls
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = { /* Cycle Theme */ },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF252930), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    Icons.Default.Palette,
                    "Theme",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF252930), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    Icons.Default.Fullscreen,
                    "Fullscreen",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DataButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier
            .width(75.dp)
            .fillMaxHeight(),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B4252))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = TextPrimary)
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 12.sp, color = TextPrimary)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.SampleButton(
    name: String,
    color: Color,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPressed) Color.White else color)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (isEditMode) onLongClick() else onClick() },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isPressed) color else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}