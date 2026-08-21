package com.example.driverappfrontend.ui.vehicle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.driverappfrontend.data.VehicleRepository
import com.example.driverappfrontend.network.ErrorParser
import com.example.driverappfrontend.network.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VehicleUiState(
    val vehicles: List<Vehicle> = emptyList(),
    val isLoading: Boolean = false,
    val isAdding: Boolean = false,
    val error: String? = null
)

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(VehicleUiState())
    val uiState: StateFlow<VehicleUiState> = _uiState.asStateFlow()

    fun loadVehicles() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.listVehicles()
                .onSuccess { vehicles -> _uiState.update { it.copy(isLoading = false, vehicles = vehicles) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = ErrorParser.extractMessage(e)) } }
        }
    }

    fun addVehicle(
        registrationNo: String?,
        make: String,
        model: String,
        gearbox: String,
        seats: Short?,
        insuranceExpiry: String?
    ) {
        _uiState.update { it.copy(isAdding = true, error = null) }
        viewModelScope.launch {
            repository.addVehicle(registrationNo, make, model, gearbox, seats, insuranceExpiry)
                .onSuccess {
                    _uiState.update { it.copy(isAdding = false) }
                    loadVehicles()
                }
                .onFailure { e -> _uiState.update { it.copy(isAdding = false, error = ErrorParser.extractMessage(e)) } }
        }
    }
}

class VehicleViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return VehicleViewModel(repository) as T
    }
}
