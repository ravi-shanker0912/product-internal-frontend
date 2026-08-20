package com.example.driverappfrontend.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.driverappfrontend.data.AuthRepository
import com.example.driverappfrontend.data.DriverRepository
import com.example.driverappfrontend.data.ProfileRepository
import com.example.driverappfrontend.data.SearchRepository
import com.example.driverappfrontend.ui.auth.AuthViewModel
import com.example.driverappfrontend.ui.auth.AuthViewModelFactory
import com.example.driverappfrontend.ui.auth.OtpEntryScreen
import com.example.driverappfrontend.ui.auth.PhoneEntryScreen
import com.example.driverappfrontend.ui.driver.DriverDocumentsScreen
import com.example.driverappfrontend.ui.driver.DriverScreen
import com.example.driverappfrontend.ui.driver.DriverViewModel
import com.example.driverappfrontend.ui.driver.DriverViewModelFactory
import com.example.driverappfrontend.ui.driver.VehiclesScreen
import com.example.driverappfrontend.ui.home.HomeScreen
import com.example.driverappfrontend.ui.profile.ProfileScreen
import com.example.driverappfrontend.ui.profile.ProfileViewModel
import com.example.driverappfrontend.ui.profile.ProfileViewModelFactory
import com.example.driverappfrontend.ui.search.SearchScreen
import com.example.driverappfrontend.ui.search.SearchViewModel
import com.example.driverappfrontend.ui.search.SearchViewModelFactory

object Routes {
    const val PHONE_ENTRY = "phone_entry"
    const val OTP_ENTRY = "otp_entry"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val DRIVER = "driver"
    const val DRIVER_DOCUMENTS = "driver_documents"
    const val VEHICLES = "vehicles"
    const val SEARCH = "search"
}

@Composable
fun AppNavGraph(
    authRepository: AuthRepository,
    driverRepository: DriverRepository,
    profileRepository: ProfileRepository,
    searchRepository: SearchRepository,
    navController: NavHostController = rememberNavController()
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))
    val driverViewModel: DriverViewModel = viewModel(factory = DriverViewModelFactory(driverRepository))
    val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(profileRepository))
    val searchViewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(searchRepository))

    NavHost(navController = navController, startDestination = Routes.PHONE_ENTRY) {
        composable(Routes.PHONE_ENTRY) {
            PhoneEntryScreen(
                viewModel = authViewModel,
                onOtpSent = { navController.navigate(Routes.OTP_ENTRY) },
                modifier = Modifier
            )
        }
        composable(Routes.OTP_ENTRY) {
            OtpEntryScreen(
                viewModel = authViewModel,
                onVerified = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PHONE_ENTRY) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }
        composable(Routes.HOME) {
            val state by authViewModel.uiState.collectAsState()
            HomeScreen(
                phone = state.phone,
                onOpenProfile = { navController.navigate(Routes.PROFILE) },
                onOpenDriver = { navController.navigate(Routes.DRIVER) },
                onOpenSearch = { navController.navigate(Routes.SEARCH) },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.PHONE_ENTRY) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                viewModel = profileViewModel,
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }
        composable(Routes.DRIVER) {
            DriverScreen(
                viewModel = driverViewModel,
                onOpenDocuments = { navController.navigate(Routes.DRIVER_DOCUMENTS) },
                onOpenVehicles = { navController.navigate(Routes.VEHICLES) },
                modifier = Modifier
            )
        }
        composable(Routes.DRIVER_DOCUMENTS) {
            DriverDocumentsScreen(
                viewModel = driverViewModel,
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }
        composable(Routes.VEHICLES) {
            VehiclesScreen(
                viewModel = driverViewModel,
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                viewModel = searchViewModel,
                onBack = { navController.popBackStack() },
                modifier = Modifier
            )
        }
    }
}
