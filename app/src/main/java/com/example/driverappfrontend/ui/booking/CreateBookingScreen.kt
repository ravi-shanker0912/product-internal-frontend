package com.example.driverappfrontend.ui.booking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.driverappfrontend.ui.common.AppTopBar
import com.example.driverappfrontend.ui.common.SectionCard
import com.example.driverappfrontend.ui.vehicle.VehicleViewModel

private val tripTypeOptions = listOf("HOURLY", "FULL_DAY", "OUTSTATION", "CAB_TRIP")

@Composable
fun CreateBookingScreen(
    viewModel: BookingViewModel,
    vehicleViewModel: VehicleViewModel,
    onBooked: (bookingId: String) -> Unit,
    onManageVehicles: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val vehicleState by vehicleViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val needsVehicle = state.pendingServiceType == "WITHOUT_CAR"

    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var locationMessage by remember { mutableStateOf<String?>(null) }
    var tripType by remember { mutableStateOf(tripTypeOptions.first()) }
    var pickupAddress by remember { mutableStateOf("") }
    var dropAddress by remember { mutableStateOf("") }
    var selectedVehicleId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(needsVehicle) { if (needsVehicle) vehicleViewModel.loadVehicles() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val result = currentLocation(context)
            lat = result?.first
            lon = result?.second
            if (result == null) locationMessage = "No location available. Enable GPS and try again."
        } else {
            locationMessage = "Location permission is required to book."
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val result = currentLocation(context)
            lat = result?.first
            lon = result?.second
            if (result == null) locationMessage = "No location available. Enable GPS and try again."
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar(title = "Confirm booking", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                if (lat != null && lon != null) {
                    Text(
                        text = "Pickup: %.4f, %.4f (your current location)".format(lat, lon),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (locationMessage != null) {
                    Text(
                        text = locationMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    "Trip type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp)
                ) {
                    tripTypeOptions.forEach { option ->
                        FilterChip(
                            selected = tripType == option,
                            onClick = { tripType = option },
                            label = { Text(option) }
                        )
                    }
                }

                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Pickup address (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )

                OutlinedTextField(
                    value = dropAddress,
                    onValueChange = { dropAddress = it },
                    label = { Text("Drop address (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                )

                if (needsVehicle) {
                    Text(
                        "Your car",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    if (vehicleState.vehicles.isEmpty()) {
                        Text(
                            "No car registered yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp)
                        ) {
                            vehicleState.vehicles.forEach { vehicle ->
                                FilterChip(
                                    selected = selectedVehicleId == vehicle.id,
                                    onClick = { selectedVehicleId = vehicle.id },
                                    label = { Text("${vehicle.make} ${vehicle.model}") }
                                )
                            }
                        }
                    }
                    TextButton(onClick = onManageVehicles, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Manage my cars")
                    }
                }

                val error = state.createError
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        val pickLat = lat ?: return@Button
                        val pickLon = lon ?: return@Button
                        viewModel.createBooking(
                            tripType = tripType,
                            pickupLat = pickLat,
                            pickupLon = pickLon,
                            pickupAddress = pickupAddress.trim().ifBlank { null },
                            dropLat = null,
                            dropLon = null,
                            dropAddress = dropAddress.trim().ifBlank { null },
                            vehicleId = selectedVehicleId,
                            onCreated = onBooked
                        )
                    },
                    enabled = lat != null && lon != null && !state.isCreating,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Book this driver")
                    }
                }
            }
        }
    }
}

private fun currentLocation(context: Context): Pair<Double, Double>? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    var location: Location? = null
    try {
        for (provider in providers) {
            if (locationManager.isProviderEnabled(provider)) {
                location = locationManager.getLastKnownLocation(provider)
                if (location != null) break
            }
        }
    } catch (e: SecurityException) {
        return null
    }
    return location?.let { it.latitude to it.longitude }
}
