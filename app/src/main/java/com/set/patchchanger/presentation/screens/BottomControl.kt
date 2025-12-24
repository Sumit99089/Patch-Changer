package com.set.patchchanger.presentation.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState
import com.set.patchchanger.ui.theme.ColorYellow
import com.set.patchchanger.ui.theme.SampleGreen
import com.set.patchchanger.ui.theme.SamplePink
import com.set.patchchanger.ui.theme.SamplePurple
import com.set.patchchanger.ui.theme.SampleTeal

@Composable
fun BottomBar(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit,
    isEditMode: Boolean
) {
    val samples = uiState.samples
    val playingSamples = uiState.playingSampleIds
    val backgroundColor = MaterialTheme.colorScheme.background

    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(backgroundColor.copy(alpha = 0.9f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Data Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DataButton("Save", Icons.Default.Save) { onEvent(MainEvent.RequestExportData) }
            DataButton("Load", Icons.Default.FolderOpen) { onEvent(MainEvent.RequestImportData) }
            DataButton(
                "Reset",
                Icons.Default.Close,
                isDestructive = true
            ) { onEvent(MainEvent.ShowResetDialog(true)) }
        }

        Spacer(Modifier.width(8.dp))

        // Sample Pads
        val defaultColors = listOf(SampleTeal, SamplePink, SampleGreen, SamplePurple)
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 0..3) {
                val sample = samples.getOrNull(i)
                val dbColor = sample?.color

                // FIXED: Removed the startsWith("#00") check to allow all valid colors
                val baseColor = if (!dbColor.isNullOrEmpty()) {
                    try {
                        Color(dbColor.toColorInt())
                    } catch (e: Exception) {
                        defaultColors[i]
                    }
                } else defaultColors[i]

                val isPlaying = playingSamples.contains(sample?.id ?: -1)

                SampleButton(
                    name = sample?.name ?: "S${i + 1}",
                    baseColor = baseColor,
                    isPlaying = isPlaying,
                    isEditMode = isEditMode,
                    onClick = { if (sample != null) onEvent(MainEvent.TriggerSample(sample.id)) },
                    onLongClick = {
                        if (sample != null) onEvent(MainEvent.ShowEditSampleDialog(sample))
                    }
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Auto-Sync Indicator
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
            val infiniteTransition = rememberInfiniteTransition(label = "sync_pulse")
            val syncColor by infiniteTransition.animateColor(
                initialValue = ColorYellow,
                targetValue = ColorYellow.copy(alpha = 0.5f),
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "syncPulse"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(syncColor)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Auto-Sync Ready",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Right Controls
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ControlButton(Icons.Default.Palette) { onEvent(MainEvent.CycleTheme) }
        }
    }
}

@Composable
fun DataButton(
    text: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isDestructive) Color(0xFFF44336) else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isDestructive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = contentColor),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        modifier = Modifier.height(48.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ControlButton(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.SampleButton(
    name: String,
    baseColor: Color,
    isPlaying: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Infinite transition for border blinking
    val infiniteTransition = rememberInfiniteTransition(label = "sample_blink")
    val blinkBorderColor by infiniteTransition.animateColor(
        initialValue = MaterialTheme.colorScheme.outline,
        targetValue = Color(0xFF39FF14), // Bright Green
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_blink"
    )

    // Apply blinking ONLY if playing
    val borderStroke = if (isPlaying) {
        BorderStroke(3.dp, blinkBorderColor)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    val finalContentColor = if (isPressed) Color.Black else Color.White

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPressed) Color.White else baseColor)
            .border(borderStroke, RoundedCornerShape(6.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { if (isEditMode) onLongClick() else onClick() },
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name,
            color = finalContentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}