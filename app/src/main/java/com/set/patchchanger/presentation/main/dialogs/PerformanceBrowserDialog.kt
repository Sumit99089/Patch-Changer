package com.set.patchchanger.presentation.main.dialogs

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.set.patchchanger.presentation.viewmodel.event.MainEvent
import com.set.patchchanger.presentation.viewmodel.state.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceBrowserDialog(
    uiState: MainUiState.Success,
    onEvent: (MainEvent) -> Unit
) {
    Dialog(
        onDismissRequest = { onEvent(MainEvent.HidePerformanceBrowser) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxSize().padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Performance Browser", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                // --- Category ---
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = uiState.performanceSelectedCategory ?: "Select Category...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        uiState.performanceCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = { onEvent(MainEvent.SelectPerformanceCategory(category)); categoryExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // --- Bank ---
                var bankExpanded by remember { mutableStateOf(false) }
                val selectedBankName = if (uiState.performanceSelectedBankIndex == -1) "Select Bank..." else uiState.performanceBanks.getOrNull(uiState.performanceSelectedBankIndex)?.name
                ExposedDropdownMenuBox(
                    expanded = bankExpanded,
                    onExpandedChange = { bankExpanded = !bankExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBankName ?: "Select Bank...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true),
                        enabled = uiState.performanceSelectedCategory != null
                    )
                    ExposedDropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        uiState.performanceBanks.forEachIndexed { index, bank ->
                            DropdownMenuItem(
                                text = { Text(bank.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = { onEvent(MainEvent.SelectPerformanceBank(index)); bankExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))

                // --- Search ---
                OutlinedTextField(
                    value = uiState.performanceSearchQuery,
                    onValueChange = { onEvent(MainEvent.UpdatePerformanceSearch(it)) },
                    label = { Text("Search performances...") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.performanceSelectedBankIndex != -1,
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                // --- List ---
                val filteredPerformances = uiState.performances.filter { it.name.contains(uiState.performanceSearchQuery, ignoreCase = true) }
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                ) {
                    if (uiState.performanceSelectedBankIndex == -1) {
                        item { Text("Please select a bank.", modifier = Modifier.padding(16.dp)) }
                    } else if (filteredPerformances.isEmpty()) {
                        item { Text("No results found.", modifier = Modifier.padding(16.dp)) }
                    }
                    items(filteredPerformances) { performance ->
                        Text(
                            text = performance.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEvent(MainEvent.SelectPerformance(performance)) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { onEvent(MainEvent.HidePerformanceBrowser) }, modifier = Modifier.align(Alignment.End)) {
                    Text("Cancel")
                }
            }
        }
    }
}