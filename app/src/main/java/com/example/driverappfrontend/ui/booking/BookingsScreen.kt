package com.example.driverappfrontend.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BookingsScreen(
    viewModel: BookingViewModel,
    onOpenBooking: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMine(asDriver = false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("My bookings", style = MaterialTheme.typography.headlineSmall)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                FilterChip(
                    selected = !state.asDriver,
                    onClick = { viewModel.loadMine(asDriver = false) },
                    label = { Text("As customer") }
                )
                FilterChip(
                    selected = state.asDriver,
                    onClick = { viewModel.loadMine(asDriver = true) },
                    label = { Text("As driver") }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            if (state.isLoadingList) {
                CircularProgressIndicator()
            } else {
                val error = state.listError
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else if (state.bookings.isEmpty()) {
                    Text("No bookings yet.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    state.bookings.forEach { booking ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            TextButton(onClick = { onOpenBooking(booking.id) }) {
                                Column {
                                    Text(booking.bookingCode, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${booking.status} · ${booking.serviceType} · ${booking.tripType}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Back")
            }
        }
    }
}
