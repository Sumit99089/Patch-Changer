package com.set.patchchanger.presentation.main.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.set.patchchanger.presentation.viewmodel.state.MainUiState

@Composable
fun BankPageNameDialog(
    state: MainUiState.Success,
    onDismiss: () -> Unit,
    onSaveBank: (String) -> Unit,
    onSavePage: (String) -> Unit
) {
    var bankName by remember { mutableStateOf(state.patchData.bankNames[state.settings.currentBankIndex]) }
    var pageName by remember { mutableStateOf(state.patchData.pageNames[state.settings.currentPageIndex]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(8.dp)) {
            Column(
                Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Edit Bank/Page Names", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = pageName,
                    onValueChange = { pageName = it },
                    label = { Text("Page Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        onSaveBank(bankName)
                        onSavePage(pageName)
                        onDismiss()
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}