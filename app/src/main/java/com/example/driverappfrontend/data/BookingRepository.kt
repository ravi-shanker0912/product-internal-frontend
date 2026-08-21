package com.example.driverappfrontend.data

import com.example.driverappfrontend.network.AcceptResponse
import com.example.driverappfrontend.network.Booking
import com.example.driverappfrontend.network.BookingApi
import com.example.driverappfrontend.network.BookingStatusHistory
import com.example.driverappfrontend.network.CancelBody
import com.example.driverappfrontend.network.CashPaymentBody
import com.example.driverappfrontend.network.CompleteBody
import com.example.driverappfrontend.network.CreateBookingBody
import com.example.driverappfrontend.network.LocationStamp
import com.example.driverappfrontend.network.Payment
import com.example.driverappfrontend.network.RateBody
import com.example.driverappfrontend.network.Rating
import com.example.driverappfrontend.network.StartBody

class BookingRepository(private val api: BookingApi) {

    suspend fun create(
        driverId: String,
        serviceType: String,
        tripType: String,
        pickupLat: Double,
        pickupLon: Double,
        pickupAddress: String?,
        dropLat: Double?,
        dropLon: Double?,
        dropAddress: String?
    ): Result<Booking> = runCatching {
        api.create(
            CreateBookingBody(
                driverId = driverId,
                serviceType = serviceType,
                tripType = tripType,
                pickupLat = pickupLat,
                pickupLon = pickupLon,
                pickupAddress = pickupAddress,
                dropLat = dropLat,
                dropLon = dropLon,
                dropAddress = dropAddress,
                vehicleId = null
            )
        )
    }

    suspend fun mine(asDriver: Boolean): Result<List<Booking>> = runCatching {
        api.mine(if (asDriver) "driver" else "customer")
    }

    suspend fun one(id: String): Result<Booking> = runCatching { api.one(id) }

    suspend fun timeline(id: String): Result<List<BookingStatusHistory>> = runCatching { api.timeline(id) }

    suspend fun accept(id: String, lat: Double?, lon: Double?): Result<AcceptResponse> = runCatching {
        api.accept(id, LocationStamp(lat, lon))
    }

    suspend fun arrived(id: String, lat: Double?, lon: Double?): Result<Booking> = runCatching {
        api.arrived(id, LocationStamp(lat, lon))
    }

    suspend fun start(id: String, otp: String): Result<Booking> = runCatching {
        api.start(id, StartBody(otp))
    }

    suspend fun complete(
        id: String,
        distanceKm: Double?,
        waitingMinutes: Int?,
        daysAway: Int?,
        nightHalts: Int?
    ): Result<Booking> = runCatching {
        api.complete(id, CompleteBody(distanceKm, waitingMinutes, daysAway, nightHalts))
    }

    suspend fun cancel(id: String, reason: String): Result<Booking> = runCatching {
        api.cancel(id, CancelBody(reason))
    }

    suspend fun rate(id: String, stars: Int, comment: String?): Result<Rating> = runCatching {
        api.rate(id, RateBody(stars, comment))
    }

    suspend fun recordCashPayment(id: String, amountPaise: Long, note: String?): Result<Payment> = runCatching {
        api.recordCashPayment(id, CashPaymentBody(amountPaise, note))
    }
}
