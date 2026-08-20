package com.example.driverappfrontend.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.driverappfrontend.data.SearchRepository
import com.example.driverappfrontend.network.ErrorParser
import com.example.driverappfrontend.network.NearbyDriver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val lat: Double? = null,
    val lon: Double? = null,
    val locationMessage: String? = null,

    val serviceType: String = "WITH_CAR", // WITH_CAR | WITHOUT_CAR
    val automaticOnly: Boolean = false,

    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val results: List<NearbyDriver> = emptyList(),
    val errorMessage: String? = null
)

class SearchViewModel(private val repository: SearchRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun setLocation(lat: Double, lon: Double) {
        _uiState.update { it.copy(lat = lat, lon = lon, locationMessage = null) }
    }

    fun reportNoLocationAvailable() {
        _uiState.update {
            it.copy(locationMessage = "No location available. Enable GPS/location on the device and try again.")
        }
    }

    fun onServiceTypeChange(value: String) {
        _uiState.update { it.copy(serviceType = value) }
    }

    fun onAutomaticOnlyChange(value: Boolean) {
        _uiState.update { it.copy(automaticOnly = value) }
    }

    fun search() {
        val state = uiState.value
        val lat = state.lat
        val lon = state.lon
        if (lat == null || lon == null) {
            reportNoLocationAvailable()
            return
        }
        _uiState.update { it.copy(isSearching = true, errorMessage = null) }
        viewModelScope.launch {
            repository.searchDrivers(lat, lon, state.serviceType, state.automaticOnly)
                .onSuccess { results ->
                    _uiState.update { it.copy(isSearching = false, hasSearched = true, results = results) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isSearching = false, hasSearched = true, errorMessage = ErrorParser.extractMessage(e))
                    }
                }
        }
    }
}

class SearchViewModelFactory(private val repository: SearchRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchViewModel(repository) as T
    }
}
