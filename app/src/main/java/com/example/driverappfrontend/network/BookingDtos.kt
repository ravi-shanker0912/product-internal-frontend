package com.example.driverappfrontend.network

data class CreateBookingBody(
    val driverId: String,
    val serviceType: String, // WITH_CAR | WITHOUT_CAR
    val tripType: String, // HOURLY | FULL_DAY | OUTSTATION | CAB_TRIP
    val pickupLat: Double,
    val pickupLon: Double,
    val pickupAddress: String?,
    val dropLat: Double?,
    val dropLon: Double?,
    val dropAddress: String?,
    val vehicleId: String?
)

data class LocationStamp(val lat: Double?, val lon: Double?)

data class StartBody(val otp: String)

data class CompleteBody(
    val distanceKm: Double?,
    val waitingMinutes: Int?,
    val daysAway: Int?,
    val nightHalts: Int?
)

data class CancelBody(val reason: String)

data class AcceptResponse(val booking: Booking, val startOtp: String)

/** Mirrors backend's Booking entity JSON. Money fields are in paise (1/100 rupee). */
data class Booking(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val bookingCode: String,
    val customerId: String,
    val driverId: String?,
    val vehicleId: String?,
    val cityId: Int,
    val serviceType: String,
    val tripType: String,
    val status: String,
    val pickupLat: Double,
    val pickupLon: Double,
    val pickupAddress: String?,
    val dropLat: Double?,
    val dropLon: Double?,
    val dropAddress: String?,
    val requestedAt: String,
    val acceptedAt: String?,
    val arrivedAt: String?,
    val startedAt: String?,
    val completedAt: String?,
    val cancelledAt: String?,
    val cancelReason: String?,
    val estimatedFarePaise: Long?,
    val billedMinutes: Int?,
    val billedKm: Double?,
    val waitingMinutes: Int,
    val totalFarePaise: Long?,
    val commissionPaise: Long?,
    val driverEarningPaise: Long?,
    val paymentMethod: String?,
    val settledAt: String?,
    val settlementRef: String?
)

data class BookingStatusHistory(
    val id: Long,
    val bookingId: String,
    val fromStatus: String?,
    val toStatus: String,
    val actorId: String?,
    val actorRole: String?,
    val lat: Double?,
    val lon: Double?,
    val note: String?,
    val createdAt: String
)
