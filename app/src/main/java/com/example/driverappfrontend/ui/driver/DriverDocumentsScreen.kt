package com.example.driverappfrontend.ui.driver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val docTypes = listOf("DL_FRONT", "DL_BACK", "SELFIE", "RC", "INSURANCE", "AADHAAR")

@Composable
fun DriverDocumentsScreen(
    viewModel: DriverViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDocuments() }

    var selectedDocType by remember { mutableStateOf(docTypes.first()) }
    var fileUrl by remember { mutableStateOf("") }
    var expiresAt by remember { mutableStateOf("") }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Documents", style = MaterialTheme.typography.headlineSmall)

            Text(
                "Document type",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp)
            ) {
                docTypes.forEach { type ->
                    FilterChip(
                        selected = selectedDocType == type,
                        onClick = { selectedDocType = type },
                        label = { Text(type) }
                    )
                }
            }

            OutlinedTextField(
                value = fileUrl,
                onValueChange = { fileUrl = it },
                label = { Text("File URL") },
                placeholder = { Text("https://...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            OutlinedTextField(
                value = expiresAt,
                onValueChange = { expiresAt = it },
                label = { Text("Expires (optional)") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            val uploadError = state.docUploadError
            if (uploadError != null) {
                Text(
                    text = uploadError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.uploadDocument(selectedDocType, fileUrl.trim(), expiresAt.trim().ifBlank { null })
                    fileUrl = ""
                    expiresAt = ""
                },
                enabled = fileUrl.isNotBlank() && !state.isUploadingDoc,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                if (state.isUploadingDoc) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Upload")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text("Uploaded documents", style = MaterialTheme.typography.titleMedium)

            if (state.isLoadingDocuments) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else if (state.documents.isEmpty()) {
                Text(
                    "No documents uploaded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                state.documents.forEach { doc ->
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text(doc.docType, style = MaterialTheme.typography.bodyLarge)
                        Text(doc.fileUrl, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Text("Back")
            }
        }
    }
}
