package com.set.patchchanger.presentation.main.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.set.patchchanger.domain.model.DisplayNameType
import com.set.patchchanger.domain.model.PatchSlot
import com.set.patchchanger.domain.model.SamplePad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotDialog(
    slot: PatchSlot,
    samples: List<SamplePad>,
    onDismiss: () -> Unit,
    onSave: (PatchSlot) -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onSwap: () -> Unit,
    onClear: () -> Unit,
    onShowPerformanceBrowser: () -> Unit
) {
    var name by remember(slot.name) { mutableStateOf(slot.name) }
    var displayNameType by remember(slot.displayNameType) { mutableStateOf(slot.displayNameType) }
    var assignedSample by remember(slot.assignedSample) { mutableIntStateOf(slot.assignedSample) }
    var colorHex by remember(slot.color) { mutableStateOf(slot.color) }
    var showColorPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Slot: ${slot.getDisplayName()}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Slot Custom Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Text("Display on Main Grid:", fontWeight = FontWeight.Bold)
                Column(Modifier.padding(top = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { displayNameType = DisplayNameType.PERFORMANCE }
                    ) {
                        RadioButton(
                            selected = displayNameType == DisplayNameType.PERFORMANCE,
                            onClick = { displayNameType = DisplayNameType.PERFORMANCE })
                        Text("Performance Name")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { displayNameType = DisplayNameType.CUSTOM }
                    ) {
                        RadioButton(
                            selected = displayNameType == DisplayNameType.CUSTOM,
                            onClick = { displayNameType = DisplayNameType.CUSTOM })
                        Text("Custom")
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text("Assigned Performance", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = slot.performanceName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Performance") },
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onShowPerformanceBrowser) {
                        Text("Select")
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text("Assign Sample Pad", fontWeight = FontWeight.Bold)
                var sampleDropdownExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = sampleDropdownExpanded,
                    onExpandedChange = { sampleDropdownExpanded = !sampleDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = samples.find { it.id == assignedSample }?.name ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sampleDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = sampleDropdownExpanded,
                        onDismissRequest = { sampleDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("None") }, onClick = {
                            assignedSample = -1
                            sampleDropdownExpanded = false
                        })
                        samples.forEach { sample ->
                            DropdownMenuItem(text = { Text(sample.name) }, onClick = {
                                assignedSample = sample.id
                                sampleDropdownExpanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text("Slot Actions", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showColorPicker = true }, modifier = Modifier.weight(1f)) { Text("Color") }
                    Button(onClick = { onCopy(); onDismiss() }, modifier = Modifier.weight(1f)) { Text("Copy") }
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onPaste(); onDismiss() }, modifier = Modifier.weight(1f)) { Text("Paste") }
                    Button(onClick = { onSwap(); onDismiss() }, modifier = Modifier.weight(1f)) { Text("Swap") }
                }
                Button(
                    onClick = { onClear(); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Clear")
                }
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(slot.copy(name = name, displayNameType = displayNameType, assignedSample = assignedSample, color = colorHex))
                        onDismiss()
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(onDismiss = { }, onColorSelected = { colorHex = it })
    }
}