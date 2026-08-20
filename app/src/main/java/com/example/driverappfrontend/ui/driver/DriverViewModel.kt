package com.example.driverappfrontend.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.driverappfrontend.data.DriverRepository
import com.example.driverappfrontend.network.DriverDocument
import com.example.driverappfrontend.network.DriverProfile
import com.example.driverappfrontend.network.ErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class DriverUiState(
    val isLoadingProfile: Boolean = true,
    val hasCheckedProfile: Boolean = false,
    val profile: DriverProfile? = null,
    val errorMessage: String? = null,

    // signup form
    val licenseNumber: String = "",
    val licenseExpiry: String = "",
    val ownsVehicle: Boolean = false,
    val canDriveAutomatic: Boolean = true,
    val isSubmitting: Boolean = false,

    val isTogglingAvailability: Boolean = false,

    val isPingingLocation: Boolean = false,
    val locationStatusMessage: String? = null,

    val documents: List<DriverDocument> = emptyList(),
    val isLoadingDocuments: Boolean = false,
    val isUploadingDoc: Boolean = false,
    val docUploadError: String? = null
)

class DriverViewModel(private val repository: DriverRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        _uiState.update { it.copy(isLoadingProfile = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getProfile()
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoadingProfile = false, hasCheckedProfile = true, profile = profile) }
                }
                .onFailure { e ->
                    if (e is HttpException && e.code() == 404) {
                        // No driver profile yet — show the signup form, not an error.
                        _uiState.update { it.copy(isLoadingProfile = false, hasCheckedProfile = true, profile = null) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingProfile = false,
                                hasCheckedProfile = true,
                                errorMessage = ErrorParser.extractMessage(e)
                            )
                        }
                    }
                }
        }
    }

    fun onLicenseNumberChange(value: String) {
        _uiState.update { it.copy(licenseNumber = value, errorMessage = null) }
    }

    fun onLicenseExpiryChange(value: String) {
        _uiState.update { it.copy(licenseExpiry = value, errorMessage = null) }
    }

    fun onOwnsVehicleChange(value: Boolean) {
        _uiState.update { it.copy(ownsVehicle = value) }
    }

    fun onCanDriveAutomaticChange(value: Boolean) {
        _uiState.update { it.copy(canDriveAutomatic = value) }
    }

    fun submitProfile() {
        val state = uiState.value
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            repository.createProfile(
                licenseNumber = state.licenseNumber.trim(),
                licenseExpiry = state.licenseExpiry.trim().ifBlank { null },
                ownsVehicle = state.ownsVehicle,
                canDriveAutomatic = state.canDriveAutomatic
            )
                .onSuccess { profile ->
                    _uiState.update { it.copy(isSubmitting = false, profile = profile) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun toggleAvailability() {
        val current = uiState.value.profile ?: return
        val goOnline = current.availability != "ONLINE"
        _uiState.update { it.copy(isTogglingAvailability = true, errorMessage = null) }
        viewModelScope.launch {
            repository.setAvailability(goOnline)
                .onSuccess { profile ->
                    _uiState.update { it.copy(isTogglingAvailability = false, profile = profile) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isTogglingAvailability = false, errorMessage = ErrorParser.extractMessage(e))
                    }
                }
        }
    }

    fun pingLocation(lat: Double, lon: Double) {
        _uiState.update { it.copy(isPingingLocation = true, locationStatusMessage = null) }
        viewModelScope.launch {
            repository.pingLocation(lat, lon, bearing = null)
                .onSuccess {
                    _uiState.update { it.copy(isPingingLocation = false, locationStatusMessage = "Location updated") }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isPingingLocation = false, locationStatusMessage = ErrorParser.extractMessage(e))
                    }
                }
        }
    }

    fun reportNoLocationAvailable() {
        _uiState.update {
            it.copy(locationStatusMessage = "No location available. Enable GPS/location on the device and try again.")
        }
    }

    fun loadDocuments() {
        _uiState.update { it.copy(isLoadingDocuments = true, docUploadError = null) }
        viewModelScope.launch {
            repository.listDocuments()
                .onSuccess { docs -> _uiState.update { it.copy(isLoadingDocuments = false, documents = docs) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoadingDocuments = false, docUploadError = ErrorParser.extractMessage(e))
                    }
                }
        }
    }

    fun uploadDocument(docType: String, fileUrl: String, expiresAt: String?) {
        _uiState.update { it.copy(isUploadingDoc = true, docUploadError = null) }
        viewModelScope.launch {
            repository.uploadDocument(docType, fileUrl, expiresAt)
                .onSuccess {
                    _uiState.update { it.copy(isUploadingDoc = false) }
                    loadDocuments()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isUploadingDoc = false, docUploadError = ErrorParser.extractMessage(e)) }
                }
        }
    }
}

class DriverViewModelFactory(private val repository: DriverRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DriverViewModel(repository) as T
    }
}
