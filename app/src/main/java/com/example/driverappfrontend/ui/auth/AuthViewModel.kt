package com.example.driverappfrontend.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.driverappfrontend.data.AuthRepository
import com.example.driverappfrontend.network.ErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Confirmed against backend application.yml: app.otp.length defaults to OTP_LENGTH=6. */
const val OTP_LENGTH = 6

data class AuthUiState(
    val phone: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, errorMessage = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value, errorMessage = null) }
    }

    fun sendOtp(onSuccess: () -> Unit) {
        val phone = uiState.value.phone.trim()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.requestOtp(phone)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = ErrorParser.extractMessage(e))
                    }
                }
        }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        val state = uiState.value
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            repository.verifyOtp(state.phone.trim(), state.otp.trim())
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = ErrorParser.extractMessage(e))
                    }
                }
        }
    }
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            _uiState.update { AuthUiState() }
            onComplete()
        }
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repository) as T
    }
}
