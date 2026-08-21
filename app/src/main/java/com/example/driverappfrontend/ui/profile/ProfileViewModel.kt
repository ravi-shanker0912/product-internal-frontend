package com.example.driverappfrontend.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.driverappfrontend.data.ProfileRepository
import com.example.driverappfrontend.network.ErrorParser
import com.example.driverappfrontend.network.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val errorMessage: String? = null,

    val fullName: String = "",
    val email: String = "",
    val cityId: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.getProfile()
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            profile = profile,
                            fullName = profile.fullName.orEmpty(),
                            email = profile.email.orEmpty(),
                            cityId = profile.cityId?.toString().orEmpty()
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, saveSuccess = false) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, saveSuccess = false) }
    }

    fun onCityIdChange(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(cityId = value, saveSuccess = false) }
        }
    }

    fun save() {
        val state = uiState.value
        _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }
        viewModelScope.launch {
            repository.updateProfile(
                fullName = state.fullName.trim().ifBlank { null },
                email = state.email.trim().ifBlank { null },
                cityId = state.cityId.trim().toIntOrNull()
            )
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(isSaving = false, profile = profile, saveSuccess = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = ErrorParser.extractMessage(e)) }
                }
        }
    }
}

class ProfileViewModelFactory(private val repository: ProfileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(repository) as T
    }
}
