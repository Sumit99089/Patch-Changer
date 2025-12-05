package com.set.patchchanger.presentation.main.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toColorInt
import com.set.patchchanger.domain.model.SamplePad

@Composable
fun EditSampleDialog(
    sample: SamplePad,
    onDismiss: () -> Unit,
    onSave: (SamplePad) -> Unit,
    onLoadFile: () -> Unit,
    onSelectFromLibrary: () -> Unit,
    onClearAudio: () -> Unit,
    onEditColor: () -> Unit
) {
    var name by remember(sample.name) { mutableStateOf(sample.name) }
    var volume by remember(sample.volume) { mutableFloatStateOf(sample.volume.toFloat()) }
    var loop by remember(sample.loop) { mutableStateOf(sample.loop) }

    val buttonColor = try {
        Color(sample.color.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Sample: $name", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Button Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Text("Audio Source", fontWeight = FontWeight.Bold)
                Text(
                    "Current: ${sample.sourceName ?: "None"}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = onLoadFile) { Text("Load New File") }
                    Button(onClick = onSelectFromLibrary) { Text("Select from Library") }
                }
                Spacer(Modifier.height(16.dp))

                Text("Button Color", fontWeight = FontWeight.Bold)
                Button(
                    onClick = onEditColor,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Color")
                }
                Spacer(Modifier.height(16.dp))

                Text("Volume: ${volume.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0f..100f,
                    steps = 99
                )
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = loop, onCheckedChange = { loop = it })
                    Text("Loop Playback")
                }
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onClearAudio,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Audio & Reset Name")
                }
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(
                            sample.copy(
                                name = name,
                                volume = volume.toInt(),
                                loop = loop
                            )
                        )
                        onDismiss()
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}