package com.example.driverappfrontend.network

/** Mirrors backend's NearbyDriver projection JSON. */
data class NearbyDriver(
    val driverId: String,
    val fullName: String?,
    val photoUrl: String?,
    val ratingAvg: Double?,
    val totalTrips: Int?,
    val ownsVehicle: Boolean?,
    val lat: Double?,
    val lon: Double?,
    val distanceKm: Double?
)
