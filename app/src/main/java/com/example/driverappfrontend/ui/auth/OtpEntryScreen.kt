package com.example.driverappfrontend.ui.auth

import android.app.Activity
import android.content.IntentFilter
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.driverappfrontend.ui.common.AppTopBar
import com.example.driverappfrontend.ui.common.SectionCard
import com.google.android.gms.auth.api.phone.SmsRetriever

@Composable
fun OtpEntryScreen(
    viewModel: AuthViewModel,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isOtpValid = state.otp.length == OTP_LENGTH && state.otp.all { it.isDigit() }
    val context = LocalContext.current

    // Shows the system "Allow <app> to read this SMS?" dialog once the SMS
    // User Consent broadcast delivers a message. On approval we get the raw
    // SMS body back and pull the OTP out of it ourselves.
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
            val otp = message?.let(::extractOtpFromMessage)
            if (otp != null) {
                viewModel.onOtpChange(otp)
                viewModel.verifyOtp(onSuccess = onVerified)
            }
        }
        // User declined the dialog, or no OTP-shaped code was found: no-op,
        // they can still type the code in manually.
    }

    // Start listening the moment this screen appears, i.e. right after
    // PhoneEntryScreen triggers onOtpSent. Listening auto-expires after five
    // minutes on the Play Services side, which comfortably covers the
    // backend's OTP TTL.
    DisposableEffect(Unit) {
        val receiver = OtpSmsReceiver { consentIntent ->
            runCatching { consentLauncher.launch(consentIntent) }
                .onFailure { Log.w("OtpEntryScreen", "Failed to launch SMS consent dialog", it) }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            ContextCompat.RECEIVER_EXPORTED
        )
        SmsRetriever.getClient(context).startSmsUserConsent(null)

        onDispose { context.unregisterReceiver(receiver) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AppTopBar(title = "Verify phone", onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            SectionCard {
                Text(
                    text = "Enter the $OTP_LENGTH-digit code",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Sent to ${state.phone}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                OutlinedTextField(
                    value = state.otp,
                    onValueChange = { value ->
                        if (value.length <= OTP_LENGTH && value.all { it.isDigit() }) {
                            viewModel.onOtpChange(value)
                        }
                    },
                    label = { Text("OTP") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Button(
                    onClick = { viewModel.verifyOtp(onSuccess = onVerified) },
                    enabled = isOtpValid && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Verify")
                    }
                }

                TextButton(
                    onClick = onBack,
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Change phone number")
                }
            }
        }
    }
}
