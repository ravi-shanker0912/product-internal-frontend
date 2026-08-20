package com.example.driverappfrontend.ui.search

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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private val serviceTypeOptions = listOf("WITH_CAR", "WITHOUT_CAR")

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchCurrentLocation(context, viewModel)
        } else {
            viewModel.reportNoLocationAvailable()
        }
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            fetchCurrentLocation(context, viewModel)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Find a driver", style = MaterialTheme.typography.headlineSmall)

            if (state.lat != null && state.lon != null) {
                Text(
                    text = "Searching near %.4f, %.4f".format(state.lat, state.lon),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            val locationMessage = state.locationMessage
            if (locationMessage != null) {
                Text(
                    text = locationMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Text(
                "Service type",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp)
            ) {
                serviceTypeOptions.forEach { option ->
                    FilterChip(
                        selected = state.serviceType == option,
                        onClick = { viewModel.onServiceTypeChange(option) },
                        label = { Text(option) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Checkbox(checked = state.automaticOnly, onCheckedChange = viewModel::onAutomaticOnlyChange)
                Text("Automatic transmission only")
            }

            val error = state.errorMessage
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = { viewModel.search() },
                enabled = state.lat != null && state.lon != null && !state.isSearching,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                if (state.isSearching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Search")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

            if (state.hasSearched && !state.isSearching) {
                if (state.results.isEmpty()) {
                    Text(
                        "No drivers nearby right now.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    state.results.forEach { driver ->
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Text(
                                driver.fullName ?: "Driver",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val distance = driver.distanceKm?.let { "%.1f km away".format(it) } ?: "Distance unknown"
                            val rating = driver.ratingAvg?.let { "★ %.1f".format(it) } ?: "No rating yet"
                            Text(
                                "$distance · $rating · ${driver.totalTrips ?: 0} trips",
                                style = MaterialTheme.typography.bodySmall
                            )
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

private fun fetchCurrentLocation(context: Context, viewModel: SearchViewModel) {
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
        viewModel.setLocation(location.latitude, location.longitude)
    } else {
        viewModel.reportNoLocationAvailable()
    }
}
