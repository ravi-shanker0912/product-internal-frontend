package com.example.driverappfrontend.ui.booking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.driverappfrontend.ui.common.AppTopBar
import com.example.driverappfrontend.ui.common.SectionCard
import com.example.driverappfrontend.ui.common.StatusBadge
import com.example.driverappfrontend.ui.common.statusTone

@Composable
fun BookingsScreen(
    viewModel: BookingViewModel,
    onOpenBooking: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMine(asDriver = false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar(title = "My bookings", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            if (state.isLoadingList) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            } else {
                val error = state.listError
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                } else if (state.bookings.isEmpty()) {
                    Text(
                        "No bookings yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.bookings.forEach { booking ->
                        SectionCard(
                            modifier = Modifier.clickable { onOpenBooking(booking.id) }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    booking.bookingCode,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                StatusBadge(text = booking.status, tone = statusTone(booking.status))
                            }
                            Text(
                                "${booking.serviceType} · ${booking.tripType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
