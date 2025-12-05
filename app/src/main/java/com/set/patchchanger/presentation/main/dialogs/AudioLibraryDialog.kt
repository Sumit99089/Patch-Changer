package com.set.patchchanger.presentation.main.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.set.patchchanger.domain.model.AudioLibraryItem

@Composable
fun AudioLibraryDialog(
    library: List<AudioLibraryItem>,
    onDismiss: () -> Unit,
    onSelect: (AudioLibraryItem) -> Unit,
    onDelete: (AudioLibraryItem) -> Unit,
    onAddFile: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<AudioLibraryItem?>(null) }
    val filteredList = library.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxHeight(0.8f)) {
            Column(Modifier.padding(16.dp)) {
                Text("Select Audio from Library", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by name...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    items(filteredList) { item ->
                        Text(
                            text = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedItem = item }
                                .background(
                                    if (selectedItem == item) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onAddFile) { Text("Add File...") }
                    Button(
                        onClick = { selectedItem?.let { onDelete(it); selectedItem = null } },
                        enabled = selectedItem != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Selected")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { selectedItem?.let { onSelect(it); onDismiss() } },
                        enabled = selectedItem != null
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}