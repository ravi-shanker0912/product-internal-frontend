package com.example.driverappfrontend.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    phone: String,
    onOpenProfile: () -> Unit,
    onOpenDriver: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenBookings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("You're logged in", style = MaterialTheme.typography.headlineSmall)
            Text(phone, style = MaterialTheme.typography.bodyMedium)

            Button(
                onClick = onOpenProfile,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Text("Profile")
            }

            Button(
                onClick = onOpenDriver,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Become a driver")
            }

            Button(
                onClick = onOpenSearch,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Find a driver")
            }

            Button(
                onClick = onOpenBookings,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("My bookings")
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("Log out")
            }
        }
    }
}
