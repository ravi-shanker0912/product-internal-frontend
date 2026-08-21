package com.example.driverappfrontend.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.driverappfrontend.data.BookingRepository
import com.example.driverappfrontend.network.Booking
import com.example.driverappfrontend.network.BookingStatusHistory
import com.example.driverappfrontend.network.ErrorParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingUiState(
    // list
    val asDriver: Boolean = false,
    val bookings: List<Booking> = emptyList(),
    val isLoadingList: Boolean = false,
    val listError: String? = null,

    // pending create (set by the search screen before navigating in)
    val pendingDriverId: String? = null,
    val pendingServiceType: String = "WITH_CAR",
    val isCreating: Boolean = false,
    val createError: String? = null,

    // detail
    val selectedBooking: Booking? = null,
    val timeline: List<BookingStatusHistory> = emptyList(),
    val isLoadingDetail: Boolean = false,
    val detailError: String? = null,
    val lastStartOtp: String? = null,

    val isActing: Boolean = false,
    val actionError: String? = null,
    val actionSuccessMessage: String? = null
)

class BookingViewModel(private val repository: BookingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val uiState: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun prepareBooking(driverId: String, serviceType: String) {
        _uiState.update { it.copy(pendingDriverId = driverId, pendingServiceType = serviceType, createError = null) }
    }

    fun createBooking(
        tripType: String,
        pickupLat: Double,
        pickupLon: Double,
        pickupAddress: String?,
        dropLat: Double?,
        dropLon: Double?,
        dropAddress: String?,
        onCreated: (String) -> Unit
    ) {
        val driverId = uiState.value.pendingDriverId ?: return
        val serviceType = uiState.value.pendingServiceType
        _uiState.update { it.copy(isCreating = true, createError = null) }
        viewModelScope.launch {
            repository.create(
                driverId = driverId,
                serviceType = serviceType,
                tripType = tripType,
                pickupLat = pickupLat,
                pickupLon = pickupLon,
                pickupAddress = pickupAddress,
                dropLat = dropLat,
                dropLon = dropLon,
                dropAddress = dropAddress
            )
                .onSuccess { booking ->
                    _uiState.update { it.copy(isCreating = false) }
                    onCreated(booking.id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isCreating = false, createError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun loadMine(asDriver: Boolean) {
        _uiState.update { it.copy(asDriver = asDriver, isLoadingList = true, listError = null) }
        viewModelScope.launch {
            repository.mine(asDriver)
                .onSuccess { bookings -> _uiState.update { it.copy(isLoadingList = false, bookings = bookings) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingList = false, listError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun loadDetail(id: String) {
        _uiState.update { it.copy(isLoadingDetail = true, detailError = null) }
        viewModelScope.launch {
            repository.one(id)
                .onSuccess { booking -> _uiState.update { it.copy(selectedBooking = booking) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingDetail = false, detailError = ErrorParser.extractMessage(e)) }
                    return@launch
                }
            repository.timeline(id)
                .onSuccess { history -> _uiState.update { it.copy(isLoadingDetail = false, timeline = history) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoadingDetail = false, detailError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun accept(id: String, lat: Double?, lon: Double?) {
        _uiState.update { it.copy(isActing = true, actionError = null, actionSuccessMessage = null) }
        viewModelScope.launch {
            repository.accept(id, lat, lon)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isActing = false,
                            selectedBooking = response.booking,
                            lastStartOtp = response.startOtp
                        )
                    }
                    loadDetail(id)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun arrived(id: String, lat: Double?, lon: Double?) {
        _uiState.update { it.copy(isActing = true, actionError = null) }
        viewModelScope.launch {
            repository.arrived(id, lat, lon)
                .onSuccess { booking -> _uiState.update { it.copy(isActing = false, selectedBooking = booking) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun start(id: String, otp: String) {
        _uiState.update { it.copy(isActing = true, actionError = null) }
        viewModelScope.launch {
            repository.start(id, otp)
                .onSuccess { booking -> _uiState.update { it.copy(isActing = false, selectedBooking = booking) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun complete(id: String, distanceKm: Double?, waitingMinutes: Int?, daysAway: Int?, nightHalts: Int?) {
        _uiState.update { it.copy(isActing = true, actionError = null) }
        viewModelScope.launch {
            repository.complete(id, distanceKm, waitingMinutes, daysAway, nightHalts)
                .onSuccess { booking -> _uiState.update { it.copy(isActing = false, selectedBooking = booking) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun cancel(id: String, reason: String) {
        _uiState.update { it.copy(isActing = true, actionError = null) }
        viewModelScope.launch {
            repository.cancel(id, reason)
                .onSuccess { booking -> _uiState.update { it.copy(isActing = false, selectedBooking = booking) } }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun rate(id: String, stars: Int, comment: String?) {
        _uiState.update { it.copy(isActing = true, actionError = null, actionSuccessMessage = null) }
        viewModelScope.launch {
            repository.rate(id, stars, comment)
                .onSuccess {
                    _uiState.update { it.copy(isActing = false, actionSuccessMessage = "Rating submitted") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }

    fun recordCashPayment(id: String, amountPaise: Long, note: String?) {
        _uiState.update { it.copy(isActing = true, actionError = null, actionSuccessMessage = null) }
        viewModelScope.launch {
            repository.recordCashPayment(id, amountPaise, note)
                .onSuccess {
                    _uiState.update { it.copy(isActing = false, actionSuccessMessage = "Cash payment recorded") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isActing = false, actionError = ErrorParser.extractMessage(e)) }
                }
        }
    }
}

class BookingViewModelFactory(private val repository: BookingRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BookingViewModel(repository) as T
    }
}
