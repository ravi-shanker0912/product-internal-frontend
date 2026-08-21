package com.example.driverappfrontend.ui.booking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.driverappfrontend.ui.profile.ProfileViewModel

@Composable
fun BookingDetailScreen(
    bookingId: String,
    viewModel: BookingViewModel,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(bookingId) { viewModel.loadDetail(bookingId) }
    // Always reload, not just when null: ProfileViewModel is nav-graph-scoped and can still
    // hold a previous account's profile after a logout/login within the same app session.
    LaunchedEffect(Unit) { profileViewModel.loadProfile() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    fun ensureLocationPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        return granted
    }

    val booking = state.selectedBooking
    val myUserId = profileState.profile?.id
    val isDriver = booking != null && myUserId != null && booking.driverId == myUserId
    val isCustomer = booking != null && myUserId != null && booking.customerId == myUserId

    var otpInput by remember { mutableStateOf("") }
    var distanceKmInput by remember { mutableStateOf("") }
    var waitingMinutesInput by remember { mutableStateOf("") }
    var cancelReason by remember { mutableStateOf("") }
    var cashAmountInput by remember { mutableStateOf("") }
    var rateStars by remember { mutableStateOf(5) }
    var rateComment by remember { mutableStateOf("") }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoadingDetail && booking == null) {
                CircularProgressIndicator()
            }
            val detailError = state.detailError
            if (detailError != null) {
                Text(text = detailError, color = MaterialTheme.colorScheme.error)
            }

            if (booking != null) {
                Text(booking.bookingCode, style = MaterialTheme.typography.headlineSmall)
                Text(
                    booking.status,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    "${booking.serviceType} · ${booking.tripType}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )

                val fare = booking.totalFarePaise ?: booking.estimatedFarePaise
                if (fare != null) {
                    val label = if (booking.totalFarePaise != null) "Fare" else "Estimated fare"
                    Text(
                        "$label: ₹%.2f".format(fare / 100.0),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                val startOtp = state.lastStartOtp
                if (startOtp != null && isDriver) {
                    Text(
                        "Start OTP: $startOtp",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Have the customer read this back to you to start the trip.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                val actionError = state.actionError
                if (actionError != null) {
                    Text(
                        text = actionError,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                val successMessage = state.actionSuccessMessage
                if (successMessage != null) {
                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Driver: accept a requested booking
                if (booking.status == "REQUESTED" && isDriver) {
                    ActionButton(
                        label = "Accept booking",
                        isLoading = state.isActing,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        ensureLocationPermission()
                        val loc = lastKnownLocation(context)
                        viewModel.accept(booking.id, loc?.first, loc?.second)
                    }
                }

                // Driver: mark arrived
                if (booking.status == "ACCEPTED" && isDriver) {
                    ActionButton(
                        label = "Mark arrived",
                        isLoading = state.isActing,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        ensureLocationPermission()
                        val loc = lastKnownLocation(context)
                        viewModel.arrived(booking.id, loc?.first, loc?.second)
                    }
                }

                // Driver: start trip with OTP
                if (booking.status == "DRIVER_ARRIVED" && isDriver) {
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) otpInput = it },
                        label = { Text("Start OTP") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                    ActionButton(
                        label = "Start trip",
                        isLoading = state.isActing,
                        enabled = otpInput.length == 4,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        viewModel.start(booking.id, otpInput)
                    }
                }

                // Driver: complete trip
                if (booking.status == "IN_PROGRESS" && isDriver) {
                    OutlinedTextField(
                        value = distanceKmInput,
                        onValueChange = { distanceKmInput = it },
                        label = { Text("Distance travelled, km (optional)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                    OutlinedTextField(
                        value = waitingMinutesInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) waitingMinutesInput = it },
                        label = { Text("Waiting minutes (optional)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    ActionButton(
                        label = "Complete trip",
                        isLoading = state.isActing,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        viewModel.complete(
                            booking.id,
                            distanceKmInput.trim().toDoubleOrNull(),
                            waitingMinutesInput.trim().toIntOrNull(),
                            null,
                            null
                        )
                    }
                }

                // Driver: record cash payment after completion
                if (booking.status == "COMPLETED" && isDriver) {
                    OutlinedTextField(
                        value = cashAmountInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) cashAmountInput = it },
                        label = { Text("Cash collected, paise") },
                        placeholder = { Text(booking.totalFarePaise?.toString() ?: "") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    )
                    ActionButton(
                        label = "Record cash payment",
                        isLoading = state.isActing,
                        enabled = cashAmountInput.isNotBlank(),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        viewModel.recordCashPayment(booking.id, cashAmountInput.trim().toLong(), null)
                    }
                }

                // Customer: rate after completion
                if (booking.status == "COMPLETED" && isCustomer) {
                    Text("Rate this trip", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        for (star in 1..5) {
                            TextButton(onClick = { rateStars = star }) {
                                Text(if (star <= rateStars) "★" else "☆", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = rateComment,
                        onValueChange = { rateComment = it },
                        label = { Text("Comment (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    ActionButton(
                        label = "Submit rating",
                        isLoading = state.isActing,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        viewModel.rate(booking.id, rateStars, rateComment.trim().ifBlank { null })
                    }
                }

                // Either party: cancel while still cancellable
                val cancellable = booking.status in setOf("REQUESTED", "ACCEPTED", "DRIVER_ARRIVED")
                if (cancellable && (isCustomer || isDriver)) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancellation reason") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { viewModel.cancel(booking.id, cancelReason.trim()) },
                        enabled = cancelReason.isNotBlank() && !state.isActing,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Cancel booking")
                    }
                }

                if (state.timeline.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                    Text("Timeline", style = MaterialTheme.typography.titleMedium)
                    state.timeline.forEach { event ->
                        Text(
                            "${event.fromStatus ?: "—"} → ${event.toStatus} (${event.actorRole ?: "system"})",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
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

@Composable
private fun ActionButton(
    label: String,
    isLoading: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth()
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(label)
        }
    }
}

private fun lastKnownLocation(context: Context): Pair<Double, Double>? {
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
