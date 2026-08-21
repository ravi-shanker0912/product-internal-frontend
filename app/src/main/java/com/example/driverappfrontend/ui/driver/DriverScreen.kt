package com.example.driverappfrontend.ui.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.driverappfrontend.ui.common.AppTopBar
import com.example.driverappfrontend.ui.common.SectionCard
import com.example.driverappfrontend.ui.common.StatusBadge
import com.example.driverappfrontend.ui.common.statusTone

@Composable
fun DriverScreen(
    viewModel: DriverViewModel,
    onOpenDocuments: () -> Unit,
    onOpenVehicles: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pingCurrentLocation(context, viewModel)
        } else {
            viewModel.reportNoLocationAvailable()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar(title = "Driver", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoadingProfile -> {
                    CircularProgressIndicator()
                }
                state.profile == null -> {
                    DriverSignupForm(state = state, viewModel = viewModel)
                }
                else -> {
                    DriverDashboard(
                        state = state,
                        viewModel = viewModel,
                        onOpenDocuments = onOpenDocuments,
                        onOpenVehicles = onOpenVehicles,
                        onUpdateLocation = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                pingCurrentLocation(context, viewModel)
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverSignupForm(state: DriverUiState, viewModel: DriverViewModel) {
    SectionCard {
        Text("Become a driver", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(
            "Tell us about your license so we can get you approved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        OutlinedTextField(
            value = state.licenseNumber,
            onValueChange = viewModel::onLicenseNumberChange,
            label = { Text("Driving licence number") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        OutlinedTextField(
            value = state.licenseExpiry,
            onValueChange = viewModel::onLicenseExpiryChange,
            label = { Text("Licence expiry") },
            placeholder = { Text("YYYY-MM-DD") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Checkbox(checked = state.ownsVehicle, onCheckedChange = viewModel::onOwnsVehicleChange)
            Text("I own my vehicle")
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = state.canDriveAutomatic, onCheckedChange = viewModel::onCanDriveAutomaticChange)
            Text("I can drive automatic")
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.submitProfile() },
            enabled = state.licenseNumber.isNotBlank() && !state.isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Submit")
            }
        }
    }
}

@Composable
private fun DriverDashboard(
    state: DriverUiState,
    viewModel: DriverViewModel,
    onOpenDocuments: () -> Unit,
    onOpenVehicles: () -> Unit,
    onUpdateLocation: () -> Unit
) {
    val profile = state.profile ?: return

    SectionCard {
        Text("Driver status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        StatusBadge(
            text = profile.verifyStatus,
            tone = statusTone(profile.verifyStatus),
            modifier = Modifier.padding(top = 8.dp)
        )
        when (profile.verifyStatus) {
            "PENDING" -> Text(
                "Your documents are awaiting review before you can go online.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            "REJECTED" -> Text(
                profile.rejectReason ?: "Your application was rejected.",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            Text("Available for trips", style = MaterialTheme.typography.bodyLarge)
            if (state.isTogglingAvailability) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Switch(
                    checked = profile.availability == "ONLINE",
                    onCheckedChange = { viewModel.toggleAvailability() }
                )
            }
        }

        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        OutlinedButton(
            onClick = onUpdateLocation,
            enabled = !state.isPingingLocation,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            if (state.isPingingLocation) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Update my location")
            }
        }
        if (state.locationStatusMessage != null) {
            Text(
                text = state.locationStatusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    SectionCard {
        Text("Manage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        OutlinedButton(
            onClick = onOpenDocuments,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Documents", modifier = Modifier.padding(start = 8.dp))
        }

        OutlinedButton(
            onClick = onOpenVehicles,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Icon(Icons.Filled.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Vehicles", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

private fun pingCurrentLocation(context: Context, viewModel: DriverViewModel) {
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
        viewModel.reportNoLocationAvailable()
        return
    }

    if (location != null) {
        viewModel.pingLocation(location.latitude, location.longitude)
    } else {
        viewModel.reportNoLocationAvailable()
    }
}
