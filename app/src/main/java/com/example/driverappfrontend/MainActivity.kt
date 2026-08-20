package com.example.driverappfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.driverappfrontend.data.AuthRepository
import com.example.driverappfrontend.data.DriverRepository
import com.example.driverappfrontend.data.TokenStore
import com.example.driverappfrontend.navigation.AppNavGraph
import com.example.driverappfrontend.network.NetworkModule
import com.example.driverappfrontend.ui.theme.DriverAppFrontendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenStore = TokenStore(applicationContext)
        NetworkModule.accessTokenProvider = { tokenStore.getAccessToken() }
        NetworkModule.refreshTokenProvider = { tokenStore.getRefreshToken() }
        NetworkModule.onTokensRefreshed = { access, refresh -> tokenStore.saveTokens(access, refresh) }
        NetworkModule.onRefreshFailed = { tokenStore.clear() }

        val authRepository = AuthRepository(NetworkModule.authApi, tokenStore, applicationContext)
        val driverRepository = DriverRepository(NetworkModule.driverApi)

        setContent {
            DriverAppFrontendTheme {
                AppNavGraph(authRepository = authRepository, driverRepository = driverRepository)
            }
        }
    }
}
