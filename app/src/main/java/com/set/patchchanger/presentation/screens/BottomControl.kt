package com.set.patchchanger.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.set.patchchanger.R
import com.set.patchchanger.domain.model.AppTheme
import com.set.patchchanger.domain.model.SamplePad
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.*

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
            .height(50.dp) // Fixed height matching screenshot
            .background(Color(0xFF101010)) // Very dark footer background
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 1. Data Management (Left)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DataButton("Save", Icons.Default.Save, Color(0xFF2d2d2d)) { onEvent(MainEvent.RequestExportData) }
            DataButton("Load", Icons.Default.FolderOpen, Color(0xFF2d2d2d)) { onEvent(MainEvent.RequestImportData) }
            DataButton("Reset", Icons.Default.Close, ColorRed) { onEvent(MainEvent.ShowResetDialog(true)) }
        }

        Spacer(Modifier.width(4.dp))

        // 2. Sample Pads (Center - Weight)
        // Hardcoded colors based on screenshot if not in DB, otherwise use DB
        val defaultColors = listOf(SampleTeal, SamplePink, SampleGreen, SamplePurple)

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 0..3) {
                val sample = samples.getOrNull(i)
                val colorHex = sample?.color ?: "#333333"
                // Fallback to screenshot specific colors if using default var
                val displayColor = try {
                    if (colorHex.startsWith("#")) Color(colorHex.toColorInt()) else defaultColors[i]
                } catch (e: Exception) { defaultColors[i] }

                SampleButton(
                    name = sample?.name ?: "S${i+1}",
                    color = displayColor,
                    isEditMode = isEditMode,
                    onClick = { if (sample != null) onEvent(MainEvent.TriggerSample(sample.id)) },
                    onLongClick = { if (sample != null) onEvent(MainEvent.ShowEditSampleDialog(sample)) }
                )
            }
        }

        Spacer(Modifier.width(4.dp))

        // 3. Bottom Right Controls
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = { /* Cycle Theme */ },
                modifier = Modifier.size(42.dp).background(DarkSurface, RoundedCornerShape(4.dp)).border(1.dp, BorderColor, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Palette, "Theme", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
            IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier.size(42.dp).background(DarkSurface, RoundedCornerShape(4.dp)).border(1.dp, BorderColor, RoundedCornerShape(4.dp))
            ) {
                Icon(Icons.Default.Fullscreen, "Fullscreen", tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun DataButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        modifier = Modifier.width(70.dp).fillMaxHeight(),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = TextPrimary)
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 11.sp, color = TextPrimary)
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
                onClick = { if(isEditMode) onLongClick() else onClick() },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isPressed) color else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}