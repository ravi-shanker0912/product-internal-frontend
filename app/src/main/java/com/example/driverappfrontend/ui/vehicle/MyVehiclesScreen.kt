package com.example.driverappfrontend.ui.vehicle

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val gearboxOptions = listOf("MANUAL", "AUTOMATIC")

@Composable
fun MyVehiclesScreen(
    viewModel: VehicleViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "My car",
    subtitle: String = "Register your car so a driver knows what they're driving.",
    addButtonLabel: String = "Add car",
    listTitle: String = "Your cars",
    emptyListLabel: String = "No cars added yet."
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadVehicles() }

    var registrationNo by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var selectedGearbox by remember { mutableStateOf(gearboxOptions.first()) }
    var seats by remember { mutableStateOf("") }
    var insuranceExpiry by remember { mutableStateOf("") }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            OutlinedTextField(
                value = registrationNo,
                onValueChange = { registrationNo = it },
                label = { Text("Registration number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("Make") },
                placeholder = { Text("e.g. Maruti") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model") },
                placeholder = { Text("e.g. Swift") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Text(
                "Gearbox",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp)
            ) {
                gearboxOptions.forEach { option ->
                    FilterChip(
                        selected = selectedGearbox == option,
                        onClick = { selectedGearbox = option },
                        label = { Text(option) }
                    )
                }
            }

            OutlinedTextField(
                value = seats,
                onValueChange = { value -> if (value.all { it.isDigit() }) seats = value },
                label = { Text("Seats (optional)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            OutlinedTextField(
                value = insuranceExpiry,
                onValueChange = { insuranceExpiry = it },
                label = { Text("Insurance expiry (optional)") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            val error = state.error
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.addVehicle(
                        registrationNo = registrationNo.trim().ifBlank { null },
                        make = make.trim(),
                        model = model.trim(),
                        gearbox = selectedGearbox,
                        seats = seats.trim().toShortOrNull(),
                        insuranceExpiry = insuranceExpiry.trim().ifBlank { null }
                    )
                    registrationNo = ""
                    make = ""
                    model = ""
                    seats = ""
                    insuranceExpiry = ""
                },
                enabled = make.isNotBlank() && model.isNotBlank() && !state.isAdding,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                if (state.isAdding) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(addButtonLabel)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            Text(listTitle, style = MaterialTheme.typography.titleMedium)

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else if (state.vehicles.isEmpty()) {
                Text(
                    emptyListLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                state.vehicles.forEach { vehicle ->
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        Text("${vehicle.make} ${vehicle.model}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${vehicle.registrationNo ?: "No reg. number"} · ${vehicle.gearbox} · ${vehicle.seats} seats",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                Text("Back")
            }
        }
    }
}
