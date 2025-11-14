package com.set.patchchanger.presentation.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.set.patchchanger.presentation.viewmodel.MainEvent
import com.set.patchchanger.presentation.viewmodel.MainUiState

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
            Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            samples.take(4).forEach { sample ->
                val color = try {
                    Color(sample.color.toColorInt())
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                Button(
                    onClick = {
                        if (isEditMode) onEvent(MainEvent.ShowEditSampleDialog(sample))
                        else onEvent(MainEvent.TriggerSample(sample.id))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    modifier = Modifier.weight(1f).padding(horizontal = 1.dp).height(40.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text(sample.name, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.White, fontSize = 10.sp)
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // Bottom Controls
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { /* Save Data Logic */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Save, "Save", modifier = Modifier.width(18.dp))
                }
                IconButton(onClick = { /* Load Data Logic */ }, modifier = Modifier.size(36.dp)) {
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
                IconButton(onClick = { /* TODO: Show Settings Dialog */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Settings, "Settings", modifier = Modifier.width(18.dp))
                }
                IconButton(onClick = { /* TODO: Show Power/Quit Dialog */ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.PowerSettingsNew, "Power", modifier = Modifier.width(18.dp))
                }
            }
        }
    }
}